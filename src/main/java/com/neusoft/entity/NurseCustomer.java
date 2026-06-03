package com.neusoft.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.List;

@Data
@TableName("nurse_customer")
public class NurseCustomer {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer nurseId;
    private Integer customerId;
    private String assignDate;
    private String remark;

    @TableField(exist = false)
    private List<Integer> customerIds;
    
    @TableField(exist = false)
    private String nurseName;
    
    @TableField(exist = false)
    private String customerName;
}
