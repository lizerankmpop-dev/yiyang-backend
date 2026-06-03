package com.neusoft.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("nurse")
public class Nurse {
    @TableId(type = IdType.AUTO)
    private Integer id;

    private String name;     // 护工姓名
    private String phone;    // 手机号
    private Integer nursingLevelId; // 护理级别ID（关联nursing_level表）
    private Integer status;  // 状态：1-在职 0-离职

    @TableLogic
    private Integer isDeleted = 0; // 0-未删除 1-已删除
}
