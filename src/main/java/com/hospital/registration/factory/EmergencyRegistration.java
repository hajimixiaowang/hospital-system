package com.hospital.registration.factory;

import com.hospital.registration.Registration;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/** 具体产品：急诊挂号单 */
@Component
public class EmergencyRegistration implements RegistrationProduct {

    @Override
    public String type() {
        return Registration.TYPE_EMERGENCY;
    }

    @Override
    public String regNoPrefix() {
        return "ER";
    }

    @Override
    public int validHours() {
        return 2;
    }

    @Override
    public BigDecimal minFee() {
        return new BigDecimal("10.00");
    }

    @Override
    public String describe() {
        return "急诊（优先处置）";
    }
}
