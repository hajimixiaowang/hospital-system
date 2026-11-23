package com.hospital.registration.fee;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 计费上下文：封装一次挂号计费需要的全部因子。
 * 策略接口只依赖这个类，将来新增因子（如季节加价）不必改动任何策略的签名。
 */
@Data
public class FeeContext {

    /** 科室基础挂号费 */
    private BigDecimal baseFee;

    /** 医生职称附加费 */
    private BigDecimal titleFee;

    /** 时段：MORNING / AFTERNOON / NIGHT */
    private String timeSlot;

    /** 患者类型：NORMAL / INSURANCE / STUDENT */
    private String patientType;

    /** 计费基数 = 科室基础费 + 职称附加费，各策略在其上做比例调整 */
    public BigDecimal getBaseAmount() {
        return baseFee.add(titleFee);
    }
}
