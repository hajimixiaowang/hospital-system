package com.hospital.notify;

import com.hospital.registration.RegistrationView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * 【设计模式·观察者】具体观察者①：站内信监听器 —— 负责人：陈梓健
 *
 * 职责单一：只负责把事件落库成站内通知。
 * 就诊相关的 REMIND / VISIT_FINISHED 事件由 RemindListener 处理，
 * 各监听器互不干涉，将来加短信、微信渠道就是再加一个实现类。
 */
@Component
public class InboxNotificationListener implements RegistrationListener {

    private static final Logger log = LoggerFactory.getLogger(InboxNotificationListener.class);

    private final NotificationService notificationService;

    public InboxNotificationListener(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    /** Spring 事件入口，转发给 onEvent（保持 GOF 观察者接口不被 Spring API 污染） */
    @EventListener
    public void onSpringEvent(RegistrationEvent event) {
        onEvent(event);
    }

    @Override
    public int onEvent(RegistrationEvent event) {
        RegistrationView r = event.getRegistration();
        switch (event.getEventType()) {
            case BOOKED:
                notificationService.insert(r.getPatientId(), EventType.BOOKED.name(),
                        "预约成功",
                        String.format("您已成功预约 %s %s（%s %s），单号 %s，挂号费 %.2f 元，请按时就诊。",
                                r.getDeptName(), r.getDoctorName(), r.getWorkDate(), slotText(r.getTimeSlot()),
                                r.getRegNo(), r.getFee()));
                log.info("[站内信] 已向患者 {} 发送预约成功通知", r.getPatientName());
                return 1;
            case CANCELLED:
                notificationService.insert(r.getPatientId(), EventType.CANCELLED.name(),
                        "预约已取消",
                        String.format("您 %s %s 的预约（单号 %s）已取消，号源已释放。",
                                r.getWorkDate(), slotText(r.getTimeSlot()), r.getRegNo()));
                log.info("[站内信] 已向患者 {} 发送取消通知", r.getPatientName());
                return 1;
            default:
                // REMIND / VISIT_FINISHED 由 RemindListener 处理，本监听器不关心（接口隔离）
                return 0;
        }
    }

    private String slotText(String slot) {
        if ("MORNING".equals(slot)) {
            return "上午";
        }
        if ("AFTERNOON".equals(slot)) {
            return "下午";
        }
        return "夜间";
    }
}
