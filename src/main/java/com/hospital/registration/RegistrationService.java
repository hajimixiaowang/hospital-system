package com.hospital.registration;

import com.hospital.common.BizException;
import com.hospital.notify.EventType;
import com.hospital.notify.RegistrationEventPublisher;
import com.hospital.payment.PaymentService;
import com.hospital.registration.fee.FeeCalculator;
import com.hospital.registration.fee.FeeContext;
import com.hospital.registration.factory.RegistrationFactory;
import com.hospital.schedule.ScheduleMapper;
import com.hospital.schedule.ScheduleView;
import com.hospital.user.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 挂号服务 —— 负责人：刘嘉钊
 *
 * 本类是三个模式的交汇点（答辩时的"模式融合"讲法）：
 *   1. 工厂方法：按 regType 把"造挂号单"委托给对应工厂（开闭——新增类型只加工厂）
 *   2. 策略：费用计算委托给 FeeCalculator 背后的策略对象（调价只加策略）
 *   3. 观察者（B 陈梓健负责）：下单/取消只"发布事件"，通知逻辑完全解耦
 */
@Service
public class RegistrationService {

    private final Map<String, RegistrationFactory> factories = new HashMap<>();
    private final RegistrationMapper registrationMapper;
    private final ScheduleMapper scheduleMapper;
    private final FeeCalculator feeCalculator;
    private final RegistrationEventPublisher eventPublisher;
    private final PaymentService paymentService;

    /** Spring 注入所有工厂实现，自动按 regType 注册（新增挂号类型本类零修改） */
    public RegistrationService(List<RegistrationFactory> factoryList,
                               RegistrationMapper registrationMapper,
                               ScheduleMapper scheduleMapper,
                               FeeCalculator feeCalculator,
                               RegistrationEventPublisher eventPublisher,
                               PaymentService paymentService) {
        for (RegistrationFactory factory : factoryList) {
            factories.put(factory.regType(), factory);
        }
        this.registrationMapper = registrationMapper;
        this.scheduleMapper = scheduleMapper;
        this.feeCalculator = feeCalculator;
        this.eventPublisher = eventPublisher;
        this.paymentService = paymentService;
    }

    /** 患者挂号：工厂造单 -> 落库 -> 发布 BOOKED 事件 */
    @Transactional
    public Registration book(User patient, ScheduleView scheduleView, String regType) {
        RegistrationFactory factory = factories.get(regType);
        if (factory == null) {
            throw new BizException("不支持的挂号类型：" + regType);
        }
        // 工厂方法模板流程内部完成：乐观占号 + 策略算费 + 组装
        Registration reg = factory.create(patient.getId(), scheduleView,
                feeCalculator, patient.getPatientType());
        // 防重复挂号：同一患者同一号源只允许挂一个号
        Long dup = registrationMapper.selectCount(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Registration>()
                        .eq("patient_id", patient.getId())
                        .eq("schedule_id", scheduleView.getId())
                        .ne("status", Registration.STATUS_CANCELLED));
        if (dup > 0) {
            throw new BizException("您已预约该号源，请勿重复挂号");
        }
        registrationMapper.insert(reg);

        // 观察者模式接入点：业务只广播"已预约"，谁关心、发几条，业务代码不感知
        eventPublisher.publish(EventType.BOOKED, buildView(reg, scheduleView, patient));
        return reg;
    }

    /** 患者取消预约：回滚号源 + 已支付则退款 + 广播 CANCELLED */
    @Transactional
    public void cancel(User patient, Long registrationId) {
        Registration reg = registrationMapper.selectById(registrationId);
        if (reg == null || !reg.getPatientId().equals(patient.getId())) {
            throw new BizException("挂号单不存在");
        }
        boolean cancellable = Registration.STATUS_BOOKED.equals(reg.getStatus())
                || Registration.STATUS_PAID.equals(reg.getStatus());
        if (!cancellable) {
            throw new BizException("该挂号单当前状态不可取消");
        }
        reg.setStatus(Registration.STATUS_CANCELLED);
        registrationMapper.updateById(reg);
        scheduleMapper.restoreRemain(reg.getScheduleId());
        paymentService.refund(registrationId);   // 已支付的挂号取消时退款

        RegistrationView view = registrationMapper.findViewById(registrationId);
        eventPublisher.publish(EventType.CANCELLED, view);
    }

    /** 页面实时预览某号源该患者的应付费用 */
    public BigDecimal previewFee(User patient, ScheduleView scheduleView) {
        FeeContext ctx = new FeeContext();
        ctx.setBaseFee(scheduleView.getBaseFee());
        ctx.setTitleFee(scheduleView.getTitleFee());
        ctx.setTimeSlot(scheduleView.getTimeSlot());
        ctx.setPatientType(patient.getPatientType());
        return feeCalculator.calcFee(ctx);
    }

    private RegistrationView buildView(Registration reg, ScheduleView sv, User patient) {
        RegistrationView v = new RegistrationView();
        v.setId(reg.getId());
        v.setPatientId(patient.getId());
        v.setRegNo(reg.getRegNo());
        v.setRegType(reg.getRegType());
        v.setFee(reg.getFee());
        v.setStatus(reg.getStatus());
        v.setCreateTime(reg.getCreateTime());
        v.setWorkDate(sv.getWorkDate());
        v.setTimeSlot(sv.getTimeSlot());
        v.setSourceNo(sv.getSourceNo());
        v.setDoctorName(sv.getDoctorName());
        v.setDeptName(sv.getDeptName());
        v.setPatientName(patient.getRealName());
        return v;
    }

    public List<RegistrationView> listByPatient(Long patientId) {
        return registrationMapper.listByPatient(patientId);
    }
}
