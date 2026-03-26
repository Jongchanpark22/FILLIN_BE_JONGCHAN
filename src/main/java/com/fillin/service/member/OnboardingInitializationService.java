package com.fillin.service.member;

import com.fillin.domain.Member;
import com.fillin.domain.NotificationSetting;
import com.fillin.domain.enums.Achievement;
import com.fillin.domain.enums.rank.Boangwan;
import com.fillin.domain.enums.rank.Haegyeolsa;
import com.fillin.domain.enums.rank.Tamheomga;
import com.fillin.global.apiPayload.code.ErrorCode;
import com.fillin.global.security.exception.AuthException;
import com.fillin.repository.NotiSetRepository;
import com.fillin.repository.member.MemberRepository;
import com.fillin.service.member.event.MemberOnboardedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 온보딩 완료 이벤트를 수신하여 초기화 작업을 비동기로 처리하는 리스너
 * 
 * 역할:
 * - MemberOnboardedEvent 수신
 * - 알림 설정 엔티티 생성
 * - 랭크 및 업적 초기화
 * 
 * 장점:
 * - OAuthServiceImpl과 느슨한 결합
 * - 이벤트 기반 아키텍처로 확장성 높음
 * - 비동기 처리로 응답 시간 단축
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OnboardingInitializationService {

    private final MemberRepository memberRepository;
    private final NotiSetRepository notiSetRepository;

    /**
     * 온보딩 완료 이벤트를 받아 초기화 작업 수행
     * 
     * @param event MemberOnboardedEvent - 온보딩 완료 이벤트
     * @throws AuthException 멤버를 찾을 수 없거나 초기화 작업 실패 시
     */
    @EventListener
    @Async("alarmExecutor")
    @Transactional
    public void handleMemberOnboarded(MemberOnboardedEvent event) {
        Long memberId = event.getMemberId();
        
        try {
            // 1. 멤버 조회 - 없으면 예외 발생
            Member member = memberRepository.findById(memberId)
                    .orElseThrow(() -> {
                        log.error("온보딩 초기화 실패: 멤버를 찾을 수 없음. memberId={}", memberId);
                        return new AuthException(ErrorCode.USER_NOT_FOUND);
                    });

            // 2. 랭크 및 업적 초기화
            member.updateAchievement(Achievement.ROOKIE);
            member.updateBoangwan(Boangwan.BOANGWAN_0);
            member.updateHaegyeolsa(Haegyeolsa.HAEGYEOLSA_0);
            member.updateTamheomga(Tamheomga.TAMHEOMGA_0);

            // 3. 알림 설정 엔티티 생성 (기본값: 모두 활성화)
            NotificationSetting noti = NotificationSetting.builder()
                    .member(member)
                    .isServiceAlarm(true)
                    .isReportAlarm(true)
                    .isFeedbackAlarm(true)
                    .build();

            notiSetRepository.save(noti);
            
            log.info("온보딩 초기화 완료: memberId={}, nickname={}", member.getId(), member.getNickname());

        } catch (AuthException e) {
            // AuthException (에러코드 기반)
            log.error("온보딩 초기화 실패 - 에러코드: {}, memberId={}", e.getErrorCode().getCode(), memberId);
            throw e; // Sentry에 자동으로 전송됨
            
        } catch (Exception e) {
            // 예상치 못한 예외 (DB 저장 실패 등)
            log.error("온보딩 초기화 중 예상치 못한 예외 발생: memberId={}", memberId, e);
            throw new RuntimeException("온보딩 초기화 실패: " + e.getMessage(), e);
        }
    }
}

