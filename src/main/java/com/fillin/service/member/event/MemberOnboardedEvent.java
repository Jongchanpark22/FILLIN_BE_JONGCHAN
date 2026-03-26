package com.fillin.service.member.event;

import com.fillin.domain.Member;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * 온보딩 완료 후 비동기로 처리할 초기화 작업을 위한 이벤트
 * - NotificationSetting 생성
 * - 랭크 및 업적 초기화
 */
@Getter
public class MemberOnboardedEvent extends ApplicationEvent {

    private final Member member;
    private final Long memberId;

    public MemberOnboardedEvent(Object source, Member member) {
        super(source);
        this.member = member;
        this.memberId = member.getId();
    }
}

