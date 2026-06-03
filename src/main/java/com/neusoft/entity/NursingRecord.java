package com.neusoft.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * 护理记录实体
 * 修复：
 * 1. 主键改为数据库自增（IdType.AUTO），移除手工 nextId() 逻辑
 * 2. recordDate/recordTime 改为 LocalDate/LocalTime，支持 DB 索引和类型安全比较
 * 3. 统一使用 Lombok @Data，消除手写 getter/setter
 */
@Data
@TableName("nursing_record")
public class NursingRecord {

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    private Integer customerId;

    private Integer nurseId;

    private Integer itemId;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate recordDate;

    @JsonFormat(pattern = "HH:mm:ss")
    private LocalTime recordTime;

    private String content;

    private String remark;

    // ===================== 新增字段（仅前端显示，非数据库字段）=====================
    /**
     * 客户姓名
     * exist = false 表示该字段不在数据库表中
     */
    @TableField(exist = false)
    private String customerName;

    /**
     * 护理人员姓名
     */
    @TableField(exist = false)
    private String nurseName;

}