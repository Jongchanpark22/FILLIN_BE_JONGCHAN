package com.fillin.service.member;

import com.fillin.domain.Member;
import com.fillin.domain.enums.SocialType;
import com.fillin.global.apiPayload.code.ErrorCode;
import com.fillin.global.security.exception.AuthException;
import com.fillin.repository.member.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 소셜 사용자 조회 및 생성 서비스
 * 책임: 기존 사용자 조회, 신규 사용자 생성
 *
 * 이 서비스는 외부 API 검증이 완료된 데이터만 받아 처리합니다.
 */
@Service
@RequiredArgsConstructor
public class OAuthUserService {

    private final MemberRepository memberRepository;

    /**
     * 소셜 타입과 ID로 기존 사용자를 조회하거나 신규 사용자를 생성합니다.
     *
     * @param socialType 소셜 타입 (KAKAO, GOOGLE, etc.)
     * @param socialId 소셜 플랫폼에서의 사용자 ID
     * @param email 소셜 플랫폼에서 얻은 이메일
     * @return 기존 또는 새로 생성된 Member
     */
    @Transactional
    public Member findOrCreateSocialMember(SocialType socialType, String socialId, String email) {
        return memberRepository
                .findBySocialTypeAndSocialId(socialType, socialId)
                .orElseGet(() -> memberRepository.save(
                        Member.createSocialMember(socialType, socialId, email)
                ));
    }

    /**
     * 멤버를 ID로 조회합니다.
     *
     * @param memberId 멤버 ID
     * @return 멤버 정보
     * @throws AuthException 멤버를 찾을 수 없는 경우
     */
    @Transactional(readOnly = true)
    public Member getMemberOrThrow(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new AuthException(ErrorCode.USER_NOT_FOUND));
    }

    /**
     * 멤버의 온보딩 상태를 확인합니다.
     *
     * @param member 확인할 멤버
     * @return 온보딩 완료 여부
     */
    public boolean isOnboarded(Member member) {
        return member.isOnboarded() && member.getNickname() != null;
    }
}

