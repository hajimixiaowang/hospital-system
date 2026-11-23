package com.hospital.notify;

import com.hospital.registration.RegistrationView;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * 【设计模式·观察者】主题（事件发布方）—— 负责人：陈梓健
 *
 * 面向抽象主题 ApplicationEventPublisher 广播，
 * 发布方完全不持有任何监听器引用（迪米特法则、低耦合）。
 */
@Component
public class RegistrationEventPublisher {

    private final ApplicationEventPublisher springPublisher;

    public RegistrationEventPublisher(ApplicationEventPublisher springPublisher) {
        this.springPublisher = springPublisher;
    }

    public void publish(EventType eventType, RegistrationView registration) {
        springPublisher.publishEvent(new RegistrationEvent(this, eventType, registration));
    }
}
