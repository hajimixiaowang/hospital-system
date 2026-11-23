package com.hospital.registration.fee;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/** 普通患者：全额收取（基数 = 科室基础费 + 职称附加费） */
@Component
public class NormalFeeStrategy implements FeeStrategy {

    @Override
    public BigDecimal calculate(FeeContext context) {
        return context.getBaseAmount().setScale(2, BigDecimal.ROUND_HALF_UP);
    }

    @Override
    public String describe() {
        return "普通患者全额计费";
    }
}
