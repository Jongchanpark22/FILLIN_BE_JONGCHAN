package com.fillin.service.member;

import com.fillin.domain.Agreement;
import com.fillin.global.apiPayload.code.ErrorCode;
import com.fillin.global.security.exception.AuthException;
import com.fillin.repository.member.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 온보딩 요청 데이터 검증 서비스
 * 책임: 이메일 중복, 닉네임 중복, 필수 약관 동의 여부 검증
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OnboardingValidationService {

    private final MemberRepository memberRepository;
    private final AgreementService agreementService;

    /**
     * 온보딩 요청의 이메일과 닉네임 중복을 검증합니다.
     *
     * @param requestEmail 요청의 이메일 (null 가능)
     * @param currentEmail 현재 멤버의 이메일
     * @param nickname 요청의 닉네임
     * @throws AuthException 중복 발견 시
     */
    public void validateEmailAndNickname(String requestEmail, String currentEmail, String nickname) {
        // 이메일 변경 요청이 있고, 현재 이메일과 다른 경우
        if (requestEmail != null && !requestEmail.equals(currentEmail)) {
            if (memberRepository.existsByEmail(requestEmail)) {
                throw new AuthException(ErrorCode.DUPLICATE_EMAIL);
            }
        }

        // 닉네임 중복 체크
        if (memberRepository.existsByNickname(nickname)) {
            throw new AuthException(ErrorCode.DUPLICATE_NICKNAME);
        }
    }

    /**
     * 필수 약관에 대한 동의 여부를 검증합니다.
     * 
     * AgreementService의 캐시된 메서드를 사용하여 성능 향상
     *
     * @param agreedIds 사용자가 동의한 약관 ID 목록
     * @throws AuthException 필수 약관에 대한 동의 누락 시
     */
    public void validateRequiredAgreements(List<Long> agreedIds) {
        if (agreedIds == null || agreedIds.isEmpty()) {
            throw new AuthException(ErrorCode.AGREEMENT_REQUIRED);
        }

        // ✅ 캐시된 메서드 사용 (DB 쿼리 1회 또는 캐시에서 바로 조회)
        List<Agreement> requiredAgreements = agreementService.getRequiredAgreements();

        for (Agreement required : requiredAgreements) {
            if (!agreedIds.contains(required.getId())) {
                throw new AuthException(ErrorCode.AGREEMENT_REQUIRED);
            }
        }
    }

    /**
     * 요청된 약관 ID들이 모두 유효한지 확인합니다.
     *
     * @param requestedAgreementIds 사용자가 동의한 약관 ID 목록
     * @throws AuthException 존재하지 않는 약관 ID 포함 시
     */
    public void validateAgreementIds(List<Long> requestedAgreementIds) {
        // ✅ 캐시된 메서드 사용 (DB 쿼리 1회 또는 캐시에서 바로 조회)
        List<Agreement> allAgreements = agreementService.getAllAgreements();
        
        long count = allAgreements.stream()
                .filter(agreement -> requestedAgreementIds.contains(agreement.getId()))
                .count();
        
        if (count != requestedAgreementIds.size()) {
            throw new AuthException(ErrorCode.AGREEMENT_NOT_FOUND);
        }
    }
}



