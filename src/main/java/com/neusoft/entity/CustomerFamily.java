package com.neusoft.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("customer_family")
public class CustomerFamily {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer customerId;
    private String name;
    private String phone;
    private String relation;
}
