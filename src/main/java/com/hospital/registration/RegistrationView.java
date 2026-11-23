package com.hospital.registration;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/** 挂号单联查视图：患者端"我的挂号"和医生端"今日预约"共用 */
@Data
public class RegistrationView {
    private Long id;
    private String regNo;
    private String regType;
    private BigDecimal fee;
    private String status;
    private LocalDateTime createTime;
    private LocalDate workDate;
    private String timeSlot;
    private String sourceNo;
    private String doctorName;     // 医生端视角下也复用此字段（患者端查的是医生名）
    private Long patientId;        // 患者用户 id（通知监听器按此发送）
    private String patientName;    // 医生端用
    private String patientPhone;   // 医生端用
    private String patientType;    // 医生端用
    private String deptName;
    private String title;
}
