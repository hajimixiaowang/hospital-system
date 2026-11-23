package com.hospital.visit.template;

import com.hospital.visit.VisitRecord;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 【设计模式·模板方法】抽象模板：就诊流程 —— 负责人：陈梓健
 *
 * 变化点（报告"因需而用"）：
 *   就诊主流程"签到→问诊→(检查)→开方→完成"的骨架对所有门诊稳定，
 *   但不同门诊类型的环节存在差异：普通门诊无需检查，专家门诊含检查，
 *   急诊优先处置且无检查。若每个类型复制一份流程代码，流程规则一调整
 *   就要同步改多处。模板方法把"骨架 + 顺序约束"固定在 final 方法里，
 *   差异下沉为钩子 needExam() 与流程列表 steps()。
 *
 * 收益（写进报告）：
 *   1. 流程骨架只维护一份（合成复用）
 *   2. 新增门诊类型 = 新增一个子类（开闭原则）
 *   3. 顺序校验由模板统一保证，子类无法跳步骤（好莱坞原则：别调用我们，我们会调用你）
 */
public abstract class VisitProcessTemplate {

    /** 钩子：本类型就诊是否包含检查环节 */
    protected abstract boolean needExam();

    /** 本类型是否优先处置（急诊列表置顶） */
    public boolean isPriority() {
        return false;
    }

    /** 本类型的流程环节序列（骨架的可变部分） */
    public List<VisitStep> steps() {
        List<VisitStep> steps = new ArrayList<>();
        steps.add(VisitStep.SIGN_IN);
        steps.add(VisitStep.INQUIRY);
        if (needExam()) {
            steps.add(VisitStep.EXAM);
        }
        steps.add(VisitStep.PRESCRIBE);
        steps.add(VisitStep.FINISH);
        return steps;
    }

    /**
     * 模板方法核心（final：子类不许改骨架逻辑）：
     * 推进一个环节。步骤越级或跳步直接拒绝，保证流程纪律。
     *
     * @param diagnosis    开方环节的诊断
     * @param prescription 开方环节的处方
     */
    public final void advance(VisitRecord record, VisitStep step,
                              String diagnosis, String prescription) {
        VisitStep expected = nextStep(record);
        if (expected == null) {
            throw new IllegalStateException("就诊流程已全部完成");
        }
        if (expected != step) {
            throw new IllegalStateException(
                    String.format("流程顺序错误：当前应执行[%s]，收到[%s]",
                            expected.getText(), step.getText()));
        }
        doAdvance(record, step, diagnosis, prescription);
    }

    /** 各环节的落地动作：写时间戳、录入诊断与处方（模板内的具体实现） */
    private void doAdvance(VisitRecord record, VisitStep step,
                           String diagnosis, String prescription) {
        LocalDateTime now = LocalDateTime.now();
        switch (step) {
            case SIGN_IN:
                record.setSignInTime(now);
                break;
            case INQUIRY:
                record.setInquiryTime(now);
                break;
            case EXAM:
                record.setExamTime(now);
                break;
            case PRESCRIBE:
                if (diagnosis == null || diagnosis.trim().isEmpty()) {
                    throw new IllegalArgumentException("开方环节必须填写诊断");
                }
                record.setDiagnosis(diagnosis);
                record.setPrescription(prescription);
                record.setPrescribeTime(now);
                break;
            case FINISH:
                record.setFinishTime(now);
                break;
            default:
                throw new IllegalArgumentException("未知环节：" + step);
        }
    }

    /** 根据各环节时间戳推断下一个应执行的环节（幂等，可反复调用） */
    public final VisitStep nextStep(VisitRecord record) {
        if (record.getSignInTime() == null) {
            return VisitStep.SIGN_IN;
        }
        if (record.getInquiryTime() == null) {
            return VisitStep.INQUIRY;
        }
        if (needExam() && record.getExamTime() == null) {
            return VisitStep.EXAM;
        }
        if (record.getPrescribeTime() == null) {
            return VisitStep.PRESCRIBE;
        }
        if (record.getFinishTime() == null) {
            return VisitStep.FINISH;
        }
        return null;
    }

    /** 当前进度描述，页面展示"已完成/待执行"用 */
    public final String currentProgress(VisitRecord record) {
        VisitStep next = nextStep(record);
        return next == null ? "已全部完成" : "下一环节：" + next.getText();
    }

    /** 模板名称（页面标注当前走的是哪套流程） */
    public abstract String describe();
}
