package com.hospital.schedule;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;

/** 号源（排班）表。source_no 由单例生成器保证全局唯一 */
@Data
@TableName("schedule")
public class Schedule {

    public static final String SLOT_MORNING = "MORNING";
    public static final String SLOT_AFTERNOON = "AFTERNOON";
    public static final String SLOT_NIGHT = "NIGHT";

    public static final String STATUS_OPEN = "OPEN";
    public static final String STATUS_CLOSED = "CLOSED";

    @TableId(type = IdType.AUTO)
    private Long id;
    private String sourceNo;    // 号源编号：NSyyyyMMdd-科室id-序号
    private Long doctorId;
    private LocalDate workDate;
    private String timeSlot;    // MORNING / AFTERNOON / NIGHT
    private Integer totalCount;
    private Integer remainCount;
    private String status;      // OPEN / CLOSED
}
