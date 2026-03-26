package com.fillin.global.security.exception;


import com.fillin.global.apiPayload.code.ErrorCode;
import com.fillin.global.apiPayload.response.Response;
import lombok.Getter;

/**
 * 비즈니스 로직 레벨에서 발생하는 인증 실패 예외
 * 
 * ⚠️ 주의: 이 클래스는 Spring Security의 AuthenticationFailureHandler와는 다릅니다.
 * 
 * 용도:
 * - 비즈니스 로직(Service, Util 등)에서 인증 검증 실패 시 사용
 * - 예: OAuth 토큰 검증 실패, 회원 정보 조회 실패 등
 * - 하지만 현재는 AuthException으로 대체되어 사용하지 않음
 * 
 * 참고: CustomAuthenticationFailureHandler는 HTTP 필터 레벨 인증 실패 처리용
 * 
 * @deprecated 현재는 AuthException을 사용합니다. 
 *             필터 레벨 인증 실패는 CustomAuthenticationFailureHandler를 사용하세요.
 */
@Getter
@Deprecated(since = "1.0", forRemoval = false)
public class AuthFailureHandler extends RuntimeException {
    private final ErrorCode errorCode;

    public AuthFailureHandler(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public Response<String> toResponse() {
        return new Response<>(false, errorCode.getCode(), errorCode.getMessage());
    }
}
