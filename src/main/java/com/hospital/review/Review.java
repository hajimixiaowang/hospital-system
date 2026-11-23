package com.hospital.review;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/** 就诊评价表 */
@Data
@TableName("review")
public class Review {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long registrationId;
    private Long patientId;
    private Long doctorId;
    private Integer rating;       // 1~5 星
    private String content;
    private LocalDateTime createTime;
}
