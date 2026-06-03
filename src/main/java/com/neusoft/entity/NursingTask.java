package com.neusoft.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("nursing_task")
public class NursingTask {
    @TableId(type = IdType.AUTO)
    private Integer id;

    private Integer nurseId;
    // 非数据库字段，必须加！
    @TableField(exist = false)
    private String nurseName;

    private Integer customerId;
    // 非数据库字段，必须加！
    @TableField(exist = false)
    private String customerName;

    private Integer levelId;
    // 非数据库字段，必须加！
    @TableField(exist = false)
    private String levelName;

    private Integer itemId;
    // 非数据库字段，必须加！
    @TableField(exist = false)
    private String itemName;

    // 非数据库字段，任务名称（兼容前端taskName字段）
    @TableField(exist = false)
    private String taskName;

    private String taskDate;

    /** 任务执行时间（如 08:30、14:00） */
    private String taskTime;

    private String status;
    private String remark;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}