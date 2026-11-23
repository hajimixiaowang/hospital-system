package com.hospital.registration.fee;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/** 夜间时段：基数上浮 20%（夜间出诊的加班成本，时段维度独立于患者类型维度） */
@Component
public class NightFeeStrategy implements FeeStrategy {

    private static final BigDecimal RATE = new BigDecimal("1.20");

    @Override
    public BigDecimal calculate(FeeContext context) {
        return context.getBaseAmount().multiply(RATE)
                .setScale(2, BigDecimal.ROUND_HALF_UP);
    }

    @Override
    public String describe() {
        return "夜间时段上浮 20%";
    }
}
