package com.hospital.registration.fee;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/** 医保患者：基数 8 折 */
@Component
public class InsuranceFeeStrategy implements FeeStrategy {

    private static final BigDecimal RATE = new BigDecimal("0.80");

    @Override
    public BigDecimal calculate(FeeContext context) {
        return context.getBaseAmount().multiply(RATE)
                .setScale(2, BigDecimal.ROUND_HALF_UP);
    }

    @Override
    public String describe() {
        return "医保患者 8 折计费";
    }
}
