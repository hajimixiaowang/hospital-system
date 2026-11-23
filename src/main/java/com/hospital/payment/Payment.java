package com.hospital.payment;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** 支付记录表（挂号后模拟支付） */
@Data
@TableName("payment")
public class Payment {

    public static final String METHOD_BALANCE = "BALANCE";
    public static final String METHOD_ALIPAY = "ALIPAY";
    public static final String METHOD_WECHAT = "WECHAT";

    public static final String STATUS_PAID = "PAID";
    public static final String STATUS_REFUNDED = "REFUNDED";

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long registrationId;
    private Long patientId;
    private BigDecimal amount;
    private String method;
    private String status;
    private LocalDateTime payTime;
}
