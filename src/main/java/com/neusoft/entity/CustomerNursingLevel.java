package com.neusoft.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("customer_nursing_level")
public class CustomerNursingLevel {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer customerId;
    private Integer levelId;
    private String startDate;
    private String remark;

    // 非数据库字段，用于前端显示
    @TableField(exist = false)
    private String customerName;

    // 非数据库字段，用于前端显示
    @TableField(exist = false)
    private String levelName;

    // 非数据库字段，护工姓名（通过 nurse_customer 表联表查询）
    @TableField(exist = false)
    private String nurseName;
}
