package com.hospital.notify;

import com.hospital.registration.RegistrationView;
import org.springframework.context.ApplicationEvent;

import java.time.LocalDateTime;

/**
 * 【设计模式·观察者】事件对象 —— 负责人：陈梓健
 *
 * 事件 = "发生了什么"的快照，与"谁关心、怎么处理"完全解耦。
 * 监听器将来怎么加（短信、微信、推送），挂号/就诊模块的代码一行都不用动。
 */
public class RegistrationEvent extends ApplicationEvent {

    private final EventType eventType;
    private final RegistrationView registration;   // 事件快照
    private final LocalDateTime occurredAt;

    public RegistrationEvent(Object source, EventType eventType, RegistrationView registration) {
        super(source);
        this.eventType = eventType;
        this.registration = registration;
        this.occurredAt = LocalDateTime.now();
    }

    public EventType getEventType() {
        return eventType;
    }

    public RegistrationView getRegistration() {
        return registration;
    }

    public LocalDateTime getOccurredAt() {
        return occurredAt;
    }
}
