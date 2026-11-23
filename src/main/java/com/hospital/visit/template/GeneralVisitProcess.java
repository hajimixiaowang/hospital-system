package com.hospital.visit.template;

import org.springframework.stereotype.Component;

/**
 * 【设计模式·模板方法】具体模板①：普通门诊流程 —— 负责人：陈梓健
 *
 * 差异点：needExam() 返回 false —— 普通门诊不做检查环节，
 * 流程自动变为 签到→问诊→开方→完成，无需复制任何流程代码。
 */
@Component
public class GeneralVisitProcess extends VisitProcessTemplate {

    @Override
    protected boolean needExam() {
        return false;
    }

    @Override
    public String describe() {
        return "普通门诊流程（无检查环节）";
    }
}
