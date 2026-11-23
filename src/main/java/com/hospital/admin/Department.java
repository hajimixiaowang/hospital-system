package com.hospital.admin;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

/** 科室表：base_fee 是挂号费计算的第一个因子 */
@Data
@TableName("department")
public class Department {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private BigDecimal baseFee;   // 科室基础挂号费
    private String intro;
}
