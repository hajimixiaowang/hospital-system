package com.hospital.knowledge;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/** AI 助手知识库（检索式问答：管理员维护，关键词匹配作答） */
@Data
@TableName("knowledge")
public class Knowledge {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String keywords;      // 命中关键词，逗号分隔
    private String answer;        // 命中后的回答
    private String category;      // 分类：挂号/费用/就诊/通用
    private LocalDateTime createTime;
}
