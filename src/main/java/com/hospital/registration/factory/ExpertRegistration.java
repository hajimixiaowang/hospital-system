package com.hospital.registration.factory;

import com.hospital.registration.Registration;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/** 具体产品：专家门诊挂号单 */
@Component
public class ExpertRegistration implements RegistrationProduct {

    @Override
    public String type() {
        return Registration.TYPE_EXPERT;
    }

    @Override
    public String regNoPrefix() {
        return "EX";
    }

    @Override
    public int validHours() {
        return 48;
    }

    @Override
    public BigDecimal minFee() {
        return new BigDecimal("20.00");
    }

    @Override
    public String describe() {
        return "专家门诊";
    }
}
