package com.hospital.visit.template;

import org.springframework.stereotype.Component;

/**
 * 【设计模式·模板方法】具体模板②：专家门诊流程 —— 负责人：陈梓健
 *
 * 差异点：needExam() 返回 true —— 专家门诊在问诊后增加检查环节，
 * 流程为 签到→问诊→检查→开方→完成。骨架未动，仅钩子取值不同。
 */
@Component
public class SpecialistVisitProcess extends VisitProcessTemplate {

    @Override
    protected boolean needExam() {
        return true;
    }

    @Override
    public String describe() {
        return "专家门诊流程（含检查环节）";
    }
}
