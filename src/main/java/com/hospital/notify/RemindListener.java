package com.hospital.notify;

import com.hospital.registration.RegistrationView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 【设计模式·观察者】具体观察者②：提醒监听器 —— 负责人：陈梓健
 *
 * 处理 REMIND（就诊提醒）与 VISIT_FINISHED（就诊完成）两类事件。
 * 与 InboxNotificationListener 的分工演示了"接口隔离 + 单一职责"：
 * 事件来了，关心的监听器干活，不关心的直接返回 0。
 *
 * 另含一个定时任务：每天早上 8 点为当天所有 BOOKED 预约补发就诊提醒
 * （幂等：同一单号只发一条），作为观察者模式之外的兜底。
 */
@Component
public class RemindListener implements RegistrationListener {

    private static final Logger log = LoggerFactory.getLogger(RemindListener.class);

    private final NotificationService notificationService;
    private final RemindQueryMapper remindQueryMapper;

    public RemindListener(NotificationService notificationService, RemindQueryMapper remindQueryMapper) {
        this.notificationService = notificationService;
        this.remindQueryMapper = remindQueryMapper;
    }

    @EventListener
    public void onSpringEvent(RegistrationEvent event) {
        onEvent(event);
    }

    @Override
    public int onEvent(RegistrationEvent event) {
        RegistrationView r = event.getRegistration();
        switch (event.getEventType()) {
            case REMIND:
                notificationService.insert(r.getPatientId(), EventType.REMIND.name(),
                        "就诊提醒",
                        String.format("提醒：您今天 %s 在 %s 有 %s %s 的预约（单号 %s），请携带有效证件按时就诊。",
                                slotText(r.getTimeSlot()), r.getDeptName(),
                                r.getDoctorName(), r.getTitle(), r.getRegNo()));
                log.info("[提醒] 已向患者 {} 发送就诊提醒", r.getPatientName());
                return 1;
            case VISIT_FINISHED:
                notificationService.insert(r.getPatientId(), EventType.VISIT_FINISHED.name(),
                        "就诊完成",
                        String.format("您在 %s 的就诊已完成，诊断与处方可在\"就诊记录\"中查看。",
                                r.getDeptName()));
                log.info("[提醒] 已向患者 {} 发送就诊完成通知", r.getPatientName());
                return 1;
            default:
                return 0;
        }
    }

    /** 定时兜底：每天 08:00 为当天预约各发一条就诊提醒（幂等） */
    @Scheduled(cron = "0 0 8 * * ?")
    public void dailyRemind() {
        int count = 0;
        for (RegistrationView r : remindQueryMapper.listTodayBooked()) {
            boolean alreadySent = remindQueryMapper.existsRemindSent(r.getRegNo()) > 0;
            if (!alreadySent) {
                notificationService.insert(r.getPatientId(), EventType.REMIND.name(),
                        "就诊提醒",
                        String.format("提醒：您今天 %s 在 %s 有预约（单号 %s），请按时就诊。",
                                slotText(r.getTimeSlot()), r.getDeptName(), r.getRegNo()));
                count++;
            }
        }
        if (count > 0) {
            log.info("[定时任务] 今晨共补发 {} 条就诊提醒", count);
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
