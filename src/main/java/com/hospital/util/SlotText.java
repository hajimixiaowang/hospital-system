package com.hospital.util;

/** 页面文案工具：英文枚举转中文显示（Thymeleaf 静态调用） */
public final class SlotText {

    private SlotText() {
    }

    public static String slot(String slot) {
        if ("MORNING".equals(slot)) {
            return "上午";
        }
        if ("AFTERNOON".equals(slot)) {
            return "下午";
        }
        return "夜间";
    }

    public static String regType(String type) {
        if ("NORMAL".equals(type)) {
            return "普通门诊";
        }
        if ("EXPERT".equals(type)) {
            return "专家门诊";
        }
        if ("EMERGENCY".equals(type)) {
            return "急诊";
        }
        return type;
    }

    public static String regStatus(String status) {
        if ("BOOKED".equals(status)) {
            return "待支付";
        }
        if ("PAID".equals(status)) {
            return "已支付";
        }
        if ("CANCELLED".equals(status)) {
            return "已取消";
        }
        if ("VISITED".equals(status)) {
            return "已就诊";
        }
        return status;
    }

    public static String patientTypeText(String type) {
        if ("INSURANCE".equals(type)) {
            return "医保患者";
        }
        if ("STUDENT".equals(type)) {
            return "学生患者";
        }
        return "普通患者";
    }

    public static String roleText(String role) {
        if ("PATIENT".equals(role)) {
            return "患者";
        }
        if ("DOCTOR".equals(role)) {
            return "医生";
        }
        if ("ADMIN".equals(role)) {
            return "管理员";
        }
        return role;
    }

    /** 判断某就诊环节是否已完成（对应时间戳已写入） */
    public static boolean isDone(com.hospital.visit.VisitRecord record, String stepName) {
        if (record == null) {
            return false;
        }
        switch (stepName) {
            case "SIGN_IN":    return record.getSignInTime() != null;
            case "INQUIRY":    return record.getInquiryTime() != null;
            case "EXAM":       return record.getExamTime() != null;
            case "PRESCRIBE":  return record.getPrescribeTime() != null;
            case "FINISH":     return record.getFinishTime() != null;
            default:           return false;
        }
    }
}
