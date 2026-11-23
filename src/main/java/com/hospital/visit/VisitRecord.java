package com.hospital.visit;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/** 就诊记录表：各环节时间戳记录流程推进轨迹（模板方法的执行痕迹） */
@Data
@TableName("visit_record")
public class VisitRecord {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long registrationId;
    private Long doctorId;
    private LocalDateTime signInTime;
    private LocalDateTime inquiryTime;
    private LocalDateTime examTime;
    private LocalDateTime prescribeTime;
    private LocalDateTime finishTime;
    private String diagnosis;
    private String prescription;
}
