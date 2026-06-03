package com.neusoft.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("reminder_message")
public class ReminderMessage {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String title;
    private String content;
    private String receiver;
    private Integer type;
    private Boolean isRead;
    private LocalDateTime createTime;
}
