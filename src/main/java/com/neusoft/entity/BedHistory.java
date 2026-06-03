package com.neusoft.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 床位历史记录实体
 * 修复：统一使用 Lombok @Data，Date → LocalDateTime
 */
@Data
@TableName("bed_history")
public class BedHistory {

    @TableId(type = IdType.AUTO)
    private Integer id;

    private Integer bedId;

    private Integer customerId;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;

    /** 类型：入住/退住/换床 */
    private String type;

    private String remark;
}
