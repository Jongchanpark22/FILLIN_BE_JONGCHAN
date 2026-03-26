package com.fillin.global.util.oauth;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fillin.dto.member.response.KakaoResponse;
import com.fillin.global.apiPayload.code.ErrorCode;
import com.fillin.global.security.exception.AuthException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
public class KakaoUtil {

    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate;

    // 카카오 사용자 정보 조회 API URL
    private static final String USER_INFO_URL = "https://kapi.kakao.com/v2/user/me";

    public KakaoUtil(ObjectMapper objectMapper, RestTemplate restTemplate) {
        this.objectMapper = objectMapper;
        this.restTemplate = restTemplate;
    }

    /** * Access Token으로 카카오 사용자 정보 요청
     */
    public KakaoResponse.KakaoProfile requestProfile(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<Void> request = new HttpEntity<>(headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    USER_INFO_URL,
                    HttpMethod.GET,
                    request,
                    String.class
            );

            if (!response.getStatusCode().is2xxSuccessful()) {
                log.error("[KAKAO] profile status: {}, body: {}", response.getStatusCode(), response.getBody());
                throw new AuthException(ErrorCode.KAKAO_AUTH_FAILED);
            }

            return objectMapper.readValue(response.getBody(), KakaoResponse.KakaoProfile.class);

        } catch (HttpClientErrorException e) {
            // 401 Unauthorized 등이 뜨면 토큰이 만료되었거나 위조된 것입니다.
            log.error("[KAKAO] Invalid Access Token: status={}, body={}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new AuthException(ErrorCode.KAKAO_AUTH_FAILED);
        } catch (JsonProcessingException e) {
            log.error("[🚨ERROR🚨] 카카오 프로필 파싱 오류: {}", e.getMessage());
            throw new AuthException(ErrorCode.KAKAO_JSON_PARSE_ERROR);
        } catch (Exception e) {
            log.error("[🚨ERROR🚨] 카카오 프로필 요청 중 오류 발생: {}", e.getMessage());
            throw new AuthException(ErrorCode.KAKAO_API_ERROR);
        }
    }
}