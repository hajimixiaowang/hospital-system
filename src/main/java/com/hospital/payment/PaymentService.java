package com.hospital.payment;

import com.hospital.common.BizException;
import com.hospital.registration.Registration;
import com.hospital.registration.RegistrationMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 支付服务：挂号后模拟支付闭环。
 * 挂号为 BOOKED（待支付），支付后变 PAID；取消时退款 REFUNDED。
 */
@Service
public class PaymentService {

    private final PaymentMapper paymentMapper;
    private final RegistrationMapper registrationMapper;

    public PaymentService(PaymentMapper paymentMapper, RegistrationMapper registrationMapper) {
        this.paymentMapper = paymentMapper;
        this.registrationMapper = registrationMapper;
    }

    @Transactional
    public Payment pay(Long patientId, Long registrationId, String method) {
        Registration reg = registrationMapper.selectById(registrationId);
        if (reg == null || !reg.getPatientId().equals(patientId)) {
            throw new BizException("挂号单不存在");
        }
        if (Registration.STATUS_PAID.equals(reg.getStatus())) {
            throw new BizException("该挂号单已支付");
        }
        if (!Registration.STATUS_BOOKED.equals(reg.getStatus())) {
            throw new BizException("该挂号单当前状态不可支付");
        }

        Payment p = new Payment();
        p.setRegistrationId(registrationId);
        p.setPatientId(patientId);
        p.setAmount(reg.getFee());
        p.setMethod(method == null ? Payment.METHOD_BALANCE : method);
        p.setStatus(Payment.STATUS_PAID);
        p.setPayTime(LocalDateTime.now());
        paymentMapper.insert(p);

        reg.setStatus(Registration.STATUS_PAID);
        registrationMapper.updateById(reg);
        return p;
    }

    /** 取消挂号时退款（若已支付） */
    @Transactional
    public void refund(Long registrationId) {
        Payment p = paymentMapper.findByRegistrationId(registrationId);
        if (p != null && Payment.STATUS_PAID.equals(p.getStatus())) {
            p.setStatus(Payment.STATUS_REFUNDED);
            paymentMapper.updateById(p);
        }
    }

    public Payment findByRegistrationId(Long registrationId) {
        return paymentMapper.findByRegistrationId(registrationId);
    }
}
