package com.hospital.registration.fee;

import com.hospital.schedule.Schedule;
import com.hospital.user.User;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 【设计模式·策略】计费上下文/策略选择器 —— 负责人：刘嘉钊
 *
 * 1. 组合多个策略对象（合成复用原则），而不是继承某个"计费基类"
 * 2. 策略选择规则集中在此，下单主流程只调用 calcFee(ctx)，完全不知道有哪几种策略
 * 3. Spring 注入所有 FeeStrategy 实现，将来新增策略类自动被发现，本类只需补一条选择规则
 */
@Component
public class FeeCalculator {

    private final Map<String, FeeStrategy> patientStrategies = new HashMap<>();
    private FeeStrategy nightStrategy;

    /** Spring 自动收集所有策略实现并按类型注册（开闭原则的注册端） */
    public FeeCalculator(List<FeeStrategy> strategies) {
        for (FeeStrategy s : strategies) {
            if (s instanceof NightFeeStrategy) {
                nightStrategy = s;
            } else if (s instanceof NormalFeeStrategy) {
                patientStrategies.put(User.PATIENT_NORMAL, s);
            } else if (s instanceof InsuranceFeeStrategy) {
                patientStrategies.put(User.PATIENT_INSURANCE, s);
            } else if (s instanceof StudentFeeStrategy) {
                patientStrategies.put(User.PATIENT_STUDENT, s);
            }
        }
    }

    /** 根据上下文选择策略并算费：夜间时段优先用夜间策略，其余按患者类型 */
    public BigDecimal calcFee(FeeContext context) {
        return select(context).calculate(context);
    }

    public FeeStrategy select(FeeContext context) {
        if (Schedule.SLOT_NIGHT.equals(context.getTimeSlot()) && nightStrategy != null) {
            return nightStrategy;
        }
        FeeStrategy strategy = patientStrategies.get(context.getPatientType());
        if (strategy == null) {
            strategy = patientStrategies.get(User.PATIENT_NORMAL);
        }
        return strategy;
    }

    /** 页面实时预览用：给出该患者该号源应付金额与说明 */
    public String previewDescription(FeeContext context) {
        return select(context).describe();
    }
}
