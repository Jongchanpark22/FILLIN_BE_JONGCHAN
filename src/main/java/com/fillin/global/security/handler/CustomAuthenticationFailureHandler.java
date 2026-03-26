package com.fillin.global.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fillin.global.apiPayload.code.ErrorCode;
import com.fillin.global.apiPayload.response.Response;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Spring Security 레벨에서 인증 실패 처리
 *
 * 역할: HTTP 필터/엔드포인트 레벨에서 인증 실패 시 JSON 응답 반환
 * 예시: OAuth 로그인 폼 제출 실패, 기본 인증 실패 등
 *
 * 사용처: SecurityConfig에 등록하여 폼 기반 인증, OAuth 인증 실패 시 호출
 */
@Slf4j
@Component
public class CustomAuthenticationFailureHandler implements AuthenticationFailureHandler {

    private final ObjectMapper objectMapper;

    public CustomAuthenticationFailureHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * Spring Security 인증 실패 핸들링
     *
     * @param request 요청
     * @param response 응답
     * @param exception 인증 실패 이유
     * @throws IOException 응답 작성 실패
     */
    @Override
    public void onAuthenticationFailure(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException exception) throws IOException {

        log.warn("[Authentication Failure] 인증 실패: {} ({})", 
            exception.getClass().getSimpleName(), exception.getMessage());

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        // ErrorCode 선택 로직
        ErrorCode errorCode = getErrorCode(exception);

        // JSON 응답 생성
        Response<String> errorResponse = new Response<>(
            false,
            errorCode.getCode(),
            errorCode.getMessage()
        );

        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }

    /**
     * AuthenticationException 타입에 따라 적절한 ErrorCode 반환
     */
    private ErrorCode getErrorCode(AuthenticationException exception) {
        String exceptionName = exception.getClass().getSimpleName();

        return switch (exceptionName) {
            case "BadCredentialsException" -> ErrorCode.INVALID_CREDENTIALS;
            case "UsernameNotFoundException" -> ErrorCode.USER_NOT_FOUND;
            case "AccountExpiredException" -> ErrorCode.ACCOUNT_EXPIRED;
            case "LockedException" -> ErrorCode.ACCOUNT_LOCKED;
            case "CredentialsExpiredException" -> ErrorCode.CREDENTIALS_EXPIRED;
            case "DisabledException" -> ErrorCode.ACCOUNT_DISABLED;
            case "InvalidGrantException" -> ErrorCode.OAUTH_INVALID_GRANT;
            default -> ErrorCode.UNAUTHORIZED;
        };
    }
}


