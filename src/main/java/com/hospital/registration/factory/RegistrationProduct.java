package com.hospital.registration.factory;

import java.math.BigDecimal;

/**
 * 【设计模式·工厂方法】抽象产品：挂号单 —— 负责人：刘嘉钊
 *
 * 不同类型挂号单的差异（编号前缀、有效时限、说明）封装在具体产品里，
 * 具体工厂负责"造哪一种"，下单主流程只面向本接口（依赖倒转）。
 */
public interface RegistrationProduct {

    /** 挂号类型标识（存库用） */
    String type();

    /** 挂号单号前缀 */
    String regNoPrefix();

    /** 挂号单有效时限（小时）：超时未就诊自动过期 */
    int validHours();

    /** 该类型的计费下限（元），工厂算费后做兜底校验 */
    BigDecimal minFee();

    /** 类型说明，页面展示用 */
    String describe();
}
