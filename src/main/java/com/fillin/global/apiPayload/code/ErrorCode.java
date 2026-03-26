package com.fillin.global.apiPayload.code;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode implements BaseCode {

    // Common Error
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON500", "서버 에러입니다. 관리자에게 문의하세요."),
    BAD_REQUEST(HttpStatus.BAD_REQUEST, "COMMON400", "잘못된 요청입니다."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "COMMON401", "인증이 필요합니다."),
    FORBIDDEN(HttpStatus.FORBIDDEN, "COMMON403", "금지된 요청입니다."),
    NOT_FOUND(HttpStatus.NOT_FOUND, "COMMON404", "찾을 수 없는 요청입니다."),

    // member Error
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER404", "사용자를 찾을 수 없습니다."),
    DUPLICATE_NICKNAME(HttpStatus.CONFLICT, "USER409", "중복된 닉네임입니다."),
    DUPLICATE_EMAIL(HttpStatus.CONFLICT, "USER408", "중복된 이메일입니다."),
    USER_NOT_AUTHENTICATED(HttpStatus.UNAUTHORIZED, "USER_401", "로그인 하지 않았습니다."),
    USER_NOT_AUTHORIZED(HttpStatus.FORBIDDEN, "USER_403", "권한이 없습니다."),
    PASSWORD_CONFIRM_MISMATCH(HttpStatus.BAD_REQUEST, "AUTH_400_PW_MISMATCH", "비밀번호가 일치하지 않습니다."),
    ALREADY_ONBOARDED_USER(HttpStatus.BAD_REQUEST, "AUTH_404", "이미 온보딩을 마친 회원입니다."),
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "AUTH_401_INVALID_CRED", "잘못된 비밀번호입니다."),
    ACCOUNT_EXPIRED(HttpStatus.UNAUTHORIZED, "AUTH_401_EXPIRED", "계정이 만료되었습니다."),
    ACCOUNT_LOCKED(HttpStatus.UNAUTHORIZED, "AUTH_401_LOCKED", "계정이 잠겨있습니다."),
    CREDENTIALS_EXPIRED(HttpStatus.UNAUTHORIZED, "AUTH_401_CRED_EXP", "자격증명이 만료되었습니다."),
    ACCOUNT_DISABLED(HttpStatus.UNAUTHORIZED, "AUTH_401_DISABLED", "계정이 비활성화되었습니다."),
    OAUTH_INVALID_GRANT(HttpStatus.UNAUTHORIZED, "OAUTH_401_GRANT", "유효하지 않은 인증 코드입니다."),


    // Report Error
    REPORT_NOT_FOUND(HttpStatus.NOT_FOUND, "REPORT404", "제보를 찾을 수 없습니다."),

    // Search Error
    INVALID_SEARCH_KEYWORD(HttpStatus.BAD_REQUEST, "SEARCH401", "검색 키워드는 필수입니다."),
    INVALID_SEARCH_TYPE(HttpStatus.BAD_REQUEST, "SEARCH402", "유효하지 않은 검색 타입입니다."),
    INVALID_LOCATION_RANGE(HttpStatus.BAD_REQUEST, "SEARCH403", "좌표 범위가 올바르지 않습니다."),
    AUTOCOMPLETE_INVALID_QUERY(HttpStatus.BAD_REQUEST, "SEARCH404", "자동완성 검색어는 필수입니다."),
    AUTOCOMPLETE_CATEGORY_NOT_FOUND(HttpStatus.BAD_REQUEST, "SEARCH405", "유효하지 않은 자동완성 카테고리입니다."),
    AUTOCOMPLETE_DATA_NOT_INITIALIZED(HttpStatus.INTERNAL_SERVER_ERROR, "SEARCH500", "자동완성 데이터가 초기화되지 않았습니다."),
    AUTOCOMPLETE_REDIS_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "SEARCH501", "자동완성 조회 중 서버 오류가 발생했습니다."),

    // Alarm Error
    ALARM_NOT_FOUND(HttpStatus.NOT_FOUND, "ALARM404", "알림을 찾을 수 없습니다."),

    // Like Error
    LIKE_FORBIDDEN(HttpStatus.FORBIDDEN, "LIKE403", "자신의 제보에는 좋아요를 누를 수 없습니다."),

    // NotiSet Error
    NOTISET_NOT_FOUND(HttpStatus.NOT_FOUND, "NOTISET404", "알림설정을 찾을 수 없습니다."),
    // jwt + Login
    JWT_GENERATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "JWT_500", "JWT 생성에 실패했습니다."),
    JWT_INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "JWT_401", "유효하지 않은 JWT 토큰입니다."),
    JWT_EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "JWT_401_EX", "만료된 JWT 토큰입니다."),
    JWT_TOKEN_NOT_FOUND(HttpStatus.UNAUTHORIZED, "JWT_401", "JWT 토큰을 찾을 수 없습니다."),
    UNSUPPORTED_SOCIAL_TYPE(HttpStatus.NOT_FOUND, "LOGIN400", "지원하지 않는 소셜타입입니다."),

    // Agreement
    AGREEMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "AGREEMENT404", "해당 약관이 존재하지 않습니다."),
    AGREEMENT_REQUIRED(HttpStatus.BAD_REQUEST, "AGREEMENT4004", "필수 약관에 모두 동의해야 합니다."),

    // KaKao
    KAKAO_JSON_PARSE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "KAKAO_500_JSON", "카카오 프로필 파싱 중 오류가 발생했습니다."),
    KAKAO_API_ERROR(HttpStatus.BAD_GATEWAY, "KAKAO_502_API", "카카오 서버와의 통신 중 오류가 발생했습니다."),
    KAKAO_INVALID_GRANT(HttpStatus.UNAUTHORIZED, "KAKAO_401_INVALID_GRANT", "유효하지 않거나 만료된 인가 코드입니다."),
    KAKAO_AUTH_FAILED(HttpStatus.UNAUTHORIZED, "KAKAO_401_AUTH_FAILED", "카카오 인증에 실패했습니다."),
    ALREADY_REGISTERED_WITH_OTHER_LOGIN(HttpStatus.CONFLICT, "AUTH_409_ALREADY_REGISTERED", "해당 이메일은 다른 로그인 방식으로 이미 가입되어 있습니다."),
    SOCIAL_LOGIN_INVALID_STATE(HttpStatus.UNAUTHORIZED, "SOCIAL_401_INVALID_GRANT", "유효하지 않거나 만료된 STATE 값입니다."),

    // Google
    GOOGLE_API_ERROR(HttpStatus.UNAUTHORIZED, "GOOGLE_400_API", "구글 서버와 통신중 오류가 발생했습니다."),
    GOOGLE_JSON_PARSE_ERROR(HttpStatus.UNAUTHORIZED, "GOOGLE_401_JSON", "구글 프로필 파싱 중 오류가 발생했습니다."),
    GOOGLE_AUTH_FAILED(HttpStatus.UNAUTHORIZED, "GOOGLE_401_AUTH_FAILED", "구글 인증에 실패했습니다.");


    private final HttpStatus status;
    private final String code;
    private final String message;

    @Override
    public HttpStatus getStatus() {
        return status;
    }

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
