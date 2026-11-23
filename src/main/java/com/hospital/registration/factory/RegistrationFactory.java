package com.hospital.registration.factory;

import com.hospital.common.BizException;
import com.hospital.registration.Registration;
import com.hospital.registration.fee.FeeCalculator;
import com.hospital.registration.fee.FeeContext;
import com.hospital.schedule.ScheduleMapper;
import com.hospital.schedule.ScheduleView;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 【设计模式·工厂方法】创建者 —— 负责人：刘嘉钊
 *
 * 变化点（报告"因需而用"）：
 *   普通/专家/急诊三类挂号单在编号前缀、有效时限、费用下限上各不相同，
 *   将来还会新增"体检预约""复诊通道"等类型。工厂方法把"造哪种挂号单"
 *   延迟到具体工厂，把"下单公共流程"（占号、算费、组装、落库）固定在
 *   本抽象类的模板里——新增类型不改公共流程（开闭），公共流程只维护
 *   一份（合成复用）。
 *
 * 与策略模式的协作点（答辩演示）：
 *   本工厂的模板流程第 3 步调用 FeeCalculator（策略）算费，
 *   "造哪一种单"由工厂决定，"收多少钱"由策略决定，两者解耦。
 */
public abstract class RegistrationFactory {

    protected final ScheduleMapper scheduleMapper;

    protected RegistrationFactory(ScheduleMapper scheduleMapper) {
        this.scheduleMapper = scheduleMapper;
    }

    /** 工厂方法核心：具体工厂决定造哪种产品 */
    protected abstract RegistrationProduct createProduct();

    /** 模板流程：校验 -> 乐观占号 -> 策略算费 -> 组装产品。落库由 Service 层事务内完成 */
    public Registration create(Long patientId, ScheduleView scheduleView,
                                FeeCalculator feeCalculator, String patientType) {
        // 1. 乐观扣号：并发抢最后一个号时只有一个请求能成功
        int updated = scheduleMapper.deductRemain(scheduleView.getId());
        if (updated == 0) {
            throw new BizException("手慢了，该号源已被约满");
        }

        // 2. 造产品：由具体工厂决定
        RegistrationProduct product = createProduct();

        // 3. 策略算费（策略模式的接入点）
        FeeContext feeContext = new FeeContext();
        feeContext.setBaseFee(scheduleView.getBaseFee());
        feeContext.setTitleFee(scheduleView.getTitleFee());
        feeContext.setTimeSlot(scheduleView.getTimeSlot());
        feeContext.setPatientType(patientType);
        BigDecimal fee = feeCalculator.calcFee(feeContext);
        if (fee.compareTo(product.minFee()) < 0) {
            fee = product.minFee();   // 类型费用下限兜底
        }

        // 4. 组装挂号单
        Registration reg = new Registration();
        reg.setRegNo(product.regNoPrefix() + LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS")));
        reg.setRegType(product.type());
        reg.setPatientId(patientId);
        reg.setScheduleId(scheduleView.getId());
        reg.setFee(fee);
        reg.setStatus(Registration.STATUS_BOOKED);
        reg.setCreateTime(LocalDateTime.now());
        return reg;
    }

    /** 产品类型标识，供 Service 按 regType 选工厂 */
    public abstract String regType();

    /** 产品说明，页面下拉框用 */
    public abstract String describe();
}
