package com.hospital.registration.fee;

import java.math.BigDecimal;

/**
 * 【设计模式·策略】计费策略接口 —— 负责人：刘嘉钊
 *
 * 变化点（写进报告"因需而用"）：
 *   医院计费规则经常调整：医保折扣、学生优惠、夜间急诊加价，将来还可能有
 *   复诊减免、节假日加价。若写成 if-else 堆在挂号流程里，每次调价都要改
 *   下单主流程并全量回归。策略模式把"可变的计费算法"各自封装成类，
 *   下单流程只依赖本接口（依赖倒转）。
 *
 * 收益：
 *   1. 新增计费策略 = 新增一个实现类 + 注册进 FeeCalculator，主流程零修改（开闭原则）
 *   2. 每个策略独立可测，调价影响面收敛到一个类（单一职责）
 */
public interface FeeStrategy {

    /** 返回该策略下应收的挂号费（元） */
    BigDecimal calculate(FeeContext context);

    /** 策略说明，页面/报告展示用 */
    String describe();
}
