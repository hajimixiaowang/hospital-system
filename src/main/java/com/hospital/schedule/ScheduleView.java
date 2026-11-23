package com.hospital.schedule;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/** 号源联查视图（号源 + 医生 + 科室），供患者端页面直接渲染 */
@Data
public class ScheduleView {
    private Long id;              // schedule.id
    private String sourceNo;
    private LocalDate workDate;
    private String timeSlot;
    private Integer totalCount;
    private Integer remainCount;
    private Long doctorId;        // doctor.id
    private Long doctorUserId;    // doctor 对应 user.id
    private String doctorName;
    private String deptName;
    private String title;         // 医生职称
    private BigDecimal baseFee;   // 科室基础挂号费（计费策略输入）
    private BigDecimal titleFee;  // 职称附加费（计费策略输入）
}
