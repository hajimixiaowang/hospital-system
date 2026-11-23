package com.hospital.user;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/** 用户表：患者 / 医生 / 管理员 三角色统一存储 */
@Data
@TableName("user")
public class User {

    /** 角色 */
    public static final String ROLE_PATIENT = "PATIENT";
    public static final String ROLE_DOCTOR = "DOCTOR";
    public static final String ROLE_ADMIN = "ADMIN";

    /** 患者类型（挂号计费策略的输入因子） */
    public static final String PATIENT_NORMAL = "NORMAL";
    public static final String PATIENT_INSURANCE = "INSURANCE";
    public static final String PATIENT_STUDENT = "STUDENT";

    /** 账号状态 */
    public static final String STATUS_ACTIVE = "ACTIVE";
    public static final String STATUS_DISABLED = "DISABLED";

    @TableId(type = IdType.AUTO)
    private Long id;
    private String username;
    private String password;      // SHA-256 摘要存储
    private String realName;
    private String phone;
    private String role;
    private String patientType;   // NORMAL / INSURANCE / STUDENT，仅患者使用
    private String status;        // ACTIVE / DISABLED
    private LocalDateTime createTime;
}
