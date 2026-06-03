package com.neusoft.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 床位实体类（严格匹配数据库表字段）
 * 数据库字段：id、bed_no、room_no、floor、status、type、remark、create_time、update_time、is_deleted
 */
@Data
@TableName("bed")
public class Bed {

    @TableId(type = IdType.AUTO)
    private Integer id;

    // 床位号（数据库字段：bed_no）
    private String bedNo;

    // 房间号（数据库字段：room_no）
    private String roomNo;

    // 楼层（数据库字段：floor）
    private String floor;

    // 床位状态：0=空闲，1=占用（数据库字段：status）
    private Integer status;

    // 床位类型（数据库字段：type）
    private String type;

    // 备注（数据库字段：remark）
    private String remark;

    // 创建时间（数据库字段：create_time）
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    // 更新时间（数据库字段：update_time）
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    // 逻辑删除（数据库字段：is_deleted）
    @TableLogic
    private Integer isDeleted = 0;
}
