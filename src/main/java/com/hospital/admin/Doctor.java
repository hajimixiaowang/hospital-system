package com.hospital.admin;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

/** 医生表：title_fee 是挂号费计算的第二个因子 */
@Data
@TableName("doctor")
public class Doctor {

    public static final String TITLE_CHIEF = "主任医师";
    public static final String TITLE_DEPUTY_CHIEF = "副主任医师";
    public static final String TITLE_ATTENDING = "主治医师";

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;          // 关联 user 表
    private Long deptId;
    private String title;         // 职称
    private BigDecimal titleFee;  // 职称附加费
    private String intro;         // 简介
    private String goodAt;        // 擅长领域
}
