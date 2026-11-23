package com.hospital.visit.template;

/** 就诊流程环节 */
public enum VisitStep {

    SIGN_IN("签到"),
    INQUIRY("问诊"),
    EXAM("检查"),
    PRESCRIBE("开方"),
    FINISH("完成");

    private final String text;

    VisitStep(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }
}
