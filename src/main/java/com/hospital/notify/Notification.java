package com.hospital.notify;

import lombok.Data;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

/** 站内通知表 */
@Data
@TableName("notification")
public class Notification {

    public static final String STATUS_UNREAD = "UNREAD";
    public static final String STATUS_READ = "READ";

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String type;         // 与 EventType 同名
    private String title;
    private String content;
    private String status;       // UNREAD / READ
    private LocalDateTime createTime;
}
