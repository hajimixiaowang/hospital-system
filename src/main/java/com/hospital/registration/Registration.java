package com.hospital.registration;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** 挂号单表。reg_type 决定由哪个工厂创建，fee 由策略模式计算 */
@Data
@TableName("registration")
public class Registration {

    /** 挂号类型（与工厂方法的产品族一一对应） */
    public static final String TYPE_NORMAL = "NORMAL";
    public static final String TYPE_EXPERT = "EXPERT";
    public static final String TYPE_EMERGENCY = "EMERGENCY";

    /** 挂号单状态 */
    public static final String STATUS_BOOKED = "BOOKED";
    public static final String STATUS_PAID = "PAID";
    public static final String STATUS_CANCELLED = "CANCELLED";
    public static final String STATUS_VISITED = "VISITED";

    @TableId(type = IdType.AUTO)
    private Long id;
    private String regNo;
    private String regType;       // NORMAL / EXPERT / EMERGENCY
    private Long patientId;      // 患者用户 id
    private Long scheduleId;     // 号源 id
    private BigDecimal fee;      // 策略模式算出的费用
    private String status;       // BOOKED / PAID / CANCELLED / VISITED
    private LocalDateTime createTime;
}
