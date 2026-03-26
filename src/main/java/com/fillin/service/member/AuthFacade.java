package com.fillin.service.member;

import com.fillin.domain.enums.SocialType;
import com.fillin.dto.member.request.SocialAuthRequest;
import com.fillin.dto.member.response.GoogleResponse;
import com.fillin.dto.member.response.KakaoResponse;
import com.fillin.dto.member.response.SocialAuthResponse;
import com.fillin.global.apiPayload.code.ErrorCode;
import com.fillin.global.security.exception.AuthException;
import com.fillin.global.util.oauth.GoogleUtil;
import com.fillin.global.util.oauth.KakaoUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuthFacade {

    private final OAuthService oAuthService; // 순수 DB 로직 담당
    private final GoogleUtil googleUtil;     // 외부 통신 담당
    private final KakaoUtil kakaoUtil;       // 외부 통신 담당

    public SocialAuthResponse login(SocialAuthRequest.LoginReq req) {

        String socialId;
        String email;
        SocialType socialType = req.getSocialType();

        // ---------------------------------------------------------
        // 1. 외부 API 통신 (트랜잭션 없이 수행 -> DB 커넥션 점유 안 함)
        //    캐싱으로 반복된 요청 제거
        // ---------------------------------------------------------
        switch (socialType) {
            case KAKAO -> {
                KakaoResponse.KakaoProfile kakaoProfile = getKakaoProfile(req.getAccessToken());
                socialId = String.valueOf(kakaoProfile.getId());
                email = (kakaoProfile.getKakaoAccount() != null) ? kakaoProfile.getKakaoAccount().getEmail() : null;
            }
            case GOOGLE -> {
                GoogleResponse.GoogleProfile googleProfile = getGoogleProfile(req.getAccessToken());
                socialId = googleProfile.getSub();
                email = googleProfile.getEmail();
            }
            default -> throw new AuthException(ErrorCode.UNSUPPORTED_SOCIAL_TYPE);
        }

        // ---------------------------------------------------------
        // 2. 검증된 정보만 들고 Service(DB 트랜잭션) 호출
        // ---------------------------------------------------------
        return oAuthService.loginOrSignup(socialType, socialId, email);
    }

    /**
     * Kakao 프로필 정보 조회 (캐시됨)
     * - 캐시 시간: 5분
     * - 캐시 키: oauthProfile:KAKAO:{accessToken}
     * 
     * @param accessToken Kakao Access Token
     * @return Kakao 프로필 정보
     */
    @Cacheable(value = "oauthProfile", key = "'KAKAO:' + #accessToken")
    public KakaoResponse.KakaoProfile getKakaoProfile(String accessToken) {
        return kakaoUtil.requestProfile(accessToken);
    }

    /**
     * Google 프로필 정보 조회 (캐시됨)
     * - 캐시 시간: 5분
     * - 캐시 키: oauthProfile:GOOGLE:{accessToken}
     * 
     * @param accessToken Google ID Token
     * @return Google 프로필 정보
     */
    @Cacheable(value = "oauthProfile", key = "'GOOGLE:' + #accessToken")
    public GoogleResponse.GoogleProfile getGoogleProfile(String accessToken) {
        return googleUtil.verifyIdToken(accessToken);
    }
}