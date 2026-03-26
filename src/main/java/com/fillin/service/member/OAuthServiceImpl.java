package com.fillin.service.member;

import com.fillin.converter.TokenResponseConverter;
import com.fillin.domain.Member;
import com.fillin.domain.enums.SocialType;
import com.fillin.dto.member.request.SocialAuthRequest;
import com.fillin.dto.member.response.SocialAuthResponse;
import com.fillin.dto.member.response.TokenResponse;
import com.fillin.global.apiPayload.code.ErrorCode;
import com.fillin.global.security.exception.AuthException;
import com.fillin.global.security.jwt.JwtTokenProvider;
import com.fillin.repository.member.MemberRepository;
import com.fillin.service.member.event.MemberOnboardedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * OAuth 로그인 및 토큰 관리 서비스
 * 
 * 책임 분리:
 * - 사용자 조회/생성: OAuthUserService
 * - 온보딩 검증: OnboardingValidationService
 * - 약관 저장: AgreementService
 * - 온보딩 초기화: OnboardingInitializationService (비동기)
 * 
 * 이 서비스는 각 서비스들을 조율하는 오케스트레이션 역할 수행
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OAuthServiceImpl implements OAuthService {

    private final MemberRepository memberRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final TokenResponseConverter tokenResponseConverter;
    // 분리된 서비스들
    private final OAuthUserService oAuthUserService;
    private final OnboardingValidationService validationService;
    private final AgreementService agreementService;
    private final ApplicationEventPublisher eventPublisher;
    /**
     * Facade에서 이미 검증된 이메일/ID를 받아 DB 작업을 수행합니다.
     * 
     * 로직:
     * 1. 기존 사용자 조회 또는 신규 사용자 생성
     * 2. 온보딩 완료 여부 확인
     * 3. 미완료 시 온보딩 토큰 발급, 완료 시 정식 토큰 발급
     */
    @Override
    @Transactional
    public SocialAuthResponse loginOrSignup(SocialType socialType, String socialId, String email) {
        // 1. 사용자 조회 또는 생성
        Member member = oAuthUserService.findOrCreateSocialMember(socialType, socialId, email);

        // 2. 온보딩 상태 확인
        if (!oAuthUserService.isOnboarded(member)) {
            String tempToken = jwtTokenProvider.createOnboardingToken(member.getId(), socialType);
            return SocialAuthResponse.builder()
                    .needOnboarding(true)
                    .tempToken(tempToken)
                    .build();
        }

        // 3. 정식 토큰 발급 (온보딩 완료된 사용자)
        String accessToken = jwtTokenProvider.createAccessToken(member, member.getEmail(), socialType);
        String refreshToken = jwtTokenProvider.createRefreshToken(member, member.getEmail(), socialType);

        member.updateRefreshToken(refreshToken); // Dirty Checking

        return SocialAuthResponse.builder()
                .needOnboarding(false)
                .token(tokenResponseConverter.toResponse(accessToken, refreshToken))
                .build();
    }

    /**
     * 온보딩 완료 처리
     * 
     * 동기 처리:
     * - 멤버 정보 검증 (중복, 이미 온보딩됨)
     * - 이메일/닉네임 저장
     * - 약관 동의 저장
     * - 온보딩 마킹
     * - 토큰 발급 및 Refresh Token 저장
     * 
     * 비동기 처리 (별도 트랜잭션):
     * - 알림 설정 엔티티 생성
     * - 랭크 및 업적 초기화
     * 
     * 이렇게 분리하여 응답 속도 개선 (블로킹 시간 단축)
     */
    @Override
    @Transactional
    public TokenResponse completeOnboarding(Long memberId, SocialAuthRequest.OnboardingReq req) {
        // 1. 멤버 존재 여부 확인
        Member member = oAuthUserService.getMemberOrThrow(memberId);

        // 2. 이미 온보딩된 사용자 확인
        if (member.isOnboarded()) {
            throw new AuthException(ErrorCode.ALREADY_ONBOARDED_USER);
        }

        // 3. 이메일 및 닉네임 중복 검증
        validationService.validateEmailAndNickname(req.getEmail(), member.getEmail(), req.getNickname());

        // 4. 필수 약관 동의 검증
        validationService.validateRequiredAgreements(req.getAgreedAgreementIds());

        // 5. 요청된 약관 ID 유효성 검증
        validationService.validateAgreementIds(req.getAgreedAgreementIds());

        // 6. 사용자 정보 업데이트 (이메일, 닉네임)
        member.updateNicknameAndEmail(req.getNickname(), req.getEmail());

        // 7. 약관 동의 저장
        agreementService.saveAgreements(member, req.getAgreedAgreementIds());

        // 8. 온보딩 완료 마킹
        member.markOnboarded();

        // 9. 토큰 발급
        SocialType socialType = member.getSocialType();
        String accessToken = jwtTokenProvider.createAccessToken(member, member.getEmail(), socialType);
        String refreshToken = jwtTokenProvider.createRefreshToken(member, member.getEmail(), socialType);
        member.updateRefreshToken(refreshToken);

        // 10. 온보딩 완료 이벤트 발행 (비동기 초기화 작업은 리스너가 처리)
        eventPublisher.publishEvent(new MemberOnboardedEvent(this, member));

        log.info("사용자 온보딩 완료: memberId={}, nickname={}", memberId, req.getNickname());

        return tokenResponseConverter.toResponse(accessToken, refreshToken);
    }

    @Transactional
    @Override
    public void logout(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new AuthException(ErrorCode.JWT_INVALID_TOKEN);
        }

        String raw = stripBearer(refreshToken);
        Long memberId = jwtTokenProvider.getMemberIdFromToken(raw);

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new AuthException(ErrorCode.USER_NOT_FOUND));

        if (member.getRefreshToken() == null || !member.getRefreshToken().equals(raw)) {
            throw new AuthException(ErrorCode.JWT_INVALID_TOKEN);
        }

        member.updateRefreshToken(null);
    }

    @Override
    @Transactional
    public TokenResponse reissue(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new AuthException(ErrorCode.JWT_INVALID_TOKEN);
        }

        String raw = stripBearer(refreshToken);

        if (!jwtTokenProvider.validateToken(raw)) {
            throw new AuthException(ErrorCode.JWT_INVALID_TOKEN);
        }

        Long memberId = jwtTokenProvider.getMemberIdFromToken(raw);

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new AuthException(ErrorCode.USER_NOT_FOUND));

        if (member.getRefreshToken() == null || !member.getRefreshToken().equals(raw)) {
            throw new AuthException(ErrorCode.JWT_INVALID_TOKEN);
        }

        SocialType socialType = member.getSocialType();
        String newAccess = jwtTokenProvider.createAccessToken(member, member.getEmail(), socialType);
        String newRefresh = jwtTokenProvider.createRefreshToken(member, member.getEmail(), socialType);
        member.updateRefreshToken(newRefresh);

        return tokenResponseConverter.toResponse(newAccess, newRefresh);
    }


    private String stripBearer(String token) {
        token = token.trim();
        return token.startsWith("Bearer ") ? token.substring(7).trim() : token;
    }
}