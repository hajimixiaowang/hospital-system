package com.hospital.registration.factory;

import com.hospital.registration.Registration;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/** 具体产品：普通门诊挂号单 */
@Component
public class NormalRegistration implements RegistrationProduct {

    @Override
    public String type() {
        return Registration.TYPE_NORMAL;
    }

    @Override
    public String regNoPrefix() {
        return "NM";
    }

    @Override
    public int validHours() {
        return 24;
    }

    @Override
    public BigDecimal minFee() {
        return BigDecimal.ZERO;
    }

    @Override
    public String describe() {
        return "普通门诊";
    }
}
