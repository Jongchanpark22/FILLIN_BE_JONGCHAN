package com.fillin.global.util.oauth;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fillin.dto.member.response.GoogleResponse;
import com.fillin.global.apiPayload.code.ErrorCode;
import com.fillin.global.security.exception.AuthException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
public class GoogleUtil {

    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate;

    private static final String TOKEN_INFO_URL = "https://oauth2.googleapis.com/tokeninfo?id_token=";

    @Value("${google.client-id}")
    private String clientId;

    public GoogleUtil(ObjectMapper objectMapper, RestTemplate restTemplate) {
        this.objectMapper = objectMapper;
        this.restTemplate = restTemplate;
    }

    public GoogleResponse.GoogleProfile verifyIdToken(String idToken) {
        try {

            // 1. 구글 서버에 ID Token 검증 요청
            String responseBody = restTemplate.getForObject(TOKEN_INFO_URL + idToken, String.class);

            // 2. 응답받은 Json 파싱
            GoogleResponse.GoogleProfile profile = objectMapper.readValue(responseBody, GoogleResponse.GoogleProfile.class);

            // 토큰의 주인(aud)이 내 앱(clientId)과 일치하는지
            if (profile.getAud() == null || !profile.getAud().equals(clientId)) {
                log.error("[GOOGLE] 내 앱의 토큰이 아닙니다. token_aud={}, my_client_id={}", profile.getAud(), clientId);
                throw new AuthException(ErrorCode.GOOGLE_AUTH_FAILED);
            }

            return profile;
        } catch (HttpClientErrorException e) {
            // 400 Bad Request 등이 뜨면 토큰이 위조되었거나 만료된 것입니다.
            log.error("[GOOGLE] Invalid ID Token: status={}, body={}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new AuthException(ErrorCode.GOOGLE_AUTH_FAILED);
        } catch (JsonProcessingException e) {
            log.error("[🚨ERROR🚨] 구글 토큰 파싱 오류: {}", e.getMessage());
            throw new AuthException(ErrorCode.GOOGLE_JSON_PARSE_ERROR);
        } catch (Exception e) {
            log.error("[🚨ERROR🚨] 구글 ID Token 검증 중 알 수 없는 오류: {}", e.getMessage());
            throw new AuthException(ErrorCode.GOOGLE_API_ERROR);
        }
    }

}