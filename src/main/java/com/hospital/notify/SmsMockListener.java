package com.hospital.notify;

import com.hospital.registration.RegistrationView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * 【设计模式·观察者】具体观察者③：模拟短信渠道 —— 负责人：陈梓健
 *
 * 扩展点演示（答辩加分讲法）：
 *   现实医院接入运营商短信网关需要资质与费用，这里只打日志模拟。
 *   它的存在证明了观察者模式"加渠道不改业务"的扩展能力——
 *   我们没有改动 RegistrationService 或任何发布方代码，仅仅
 *   新增了这个 @Component，系统就"多了一个通知渠道"。
 */
@Component
public class SmsMockListener implements RegistrationListener {

    private static final Logger log = LoggerFactory.getLogger(SmsMockListener.class);

    @EventListener
    public void onSpringEvent(RegistrationEvent event) {
        onEvent(event);
    }

    @Override
    public int onEvent(RegistrationEvent event) {
        RegistrationView r = event.getRegistration();
        if (event.getEventType() == EventType.VISIT_FINISHED) {
            return 0;   // 就诊完成不发短信，避免打扰（渠道自己的过滤策略）
        }
        log.info("[模拟短信] 尊敬的{}：您的预约（单号{}）状态变更为{}，详情请查看App。",
                r.getPatientName(), r.getRegNo(), event.getEventType());
        return 1;
    }
}
