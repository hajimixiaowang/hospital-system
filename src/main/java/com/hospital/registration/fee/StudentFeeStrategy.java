package com.hospital.registration.fee;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/** 学生患者：基数 5 折 */
@Component
public class StudentFeeStrategy implements FeeStrategy {

    private static final BigDecimal RATE = new BigDecimal("0.50");

    @Override
    public BigDecimal calculate(FeeContext context) {
        return context.getBaseAmount().multiply(RATE)
                .setScale(2, BigDecimal.ROUND_HALF_UP);
    }

    @Override
    public String describe() {
        return "学生患者 5 折计费";
    }
}
