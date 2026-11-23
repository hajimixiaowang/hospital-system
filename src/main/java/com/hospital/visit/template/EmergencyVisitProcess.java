package com.hospital.visit.template;

import org.springframework.stereotype.Component;

/**
 * 【设计模式·模板方法】具体模板③：急诊流程 —— 负责人：陈梓健
 *
 * 差异点：
 *   1. needExam() 返回 false —— 急诊检查在问诊中一并完成，不单列环节
 *   2. isPriority() 返回 true —— 医生端预约列表将急诊置顶（优先处置）
 * 骨架依然未动：模板方法模式的价值在这里体现得最清楚。
 */
@Component
public class EmergencyVisitProcess extends VisitProcessTemplate {

    @Override
    protected boolean needExam() {
        return false;
    }

    @Override
    public boolean isPriority() {
        return true;
    }

    @Override
    public String describe() {
        return "急诊流程（优先处置、无检查环节）";
    }
}
