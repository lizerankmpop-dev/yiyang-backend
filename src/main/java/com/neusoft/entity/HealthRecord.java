package com.neusoft.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 健康档案实体类
 * 用于记录老人的血压、血糖等健康数据
 */
@Data
@TableName("health_record") // 请确保数据库表名与此一致
public class HealthRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 关联的老人ID
     */
    private Long customerId;

    /**
     * 收缩压（高压）
     */
    private Integer systolicPressure;

    /**
     * 舒张压（低压）
     */
    private Integer diastolicPressure;

    /**
     * 血糖值
     */
    private Double bloodSugar;

    /**
     * 测量时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime measureTime;

    /**
     * 记录人ID
     */
    private Long recorderId;

    /**
     * 备注
     */
    private String remark;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    /**
     * 逻辑删除标志（0=未删除，1=已删除）
     */
    private Integer isDeleted;
}