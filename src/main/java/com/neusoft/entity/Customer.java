package com.neusoft.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("customer")
public class Customer {
    @TableId(type = IdType.AUTO)
    private Integer id;

    private String name;
    private Integer age;
    private String gender;

    private Integer bedId;
    private Integer levelId;

    private String phone;
    private String tags;
    private String avatar;
    private String report;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate checkInDate;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime checkOutTime;
    private LocalDateTime goOutStart;
    private LocalDateTime goOutEnd;
    private Integer isOuting;
    private Integer isCheckIn;

    private String idcard;
    private String bloodType;
    private LocalDate birthday;
    private String buildingNo;
    private String roomNo;
    private LocalDate expirationDate;
    private Integer userId;
    private String psychosomaticState;
    private BigDecimal height;
    private BigDecimal weight;
    private Integer isDeleted;

    @TableField(exist = false)
    private String bedNo;

    @TableField(exist = false)
    private String nurseLevel;

    @TableField(exist = false)
    private Integer pendingBackdownId;

    @TableField(exist = false)
    private String emergencyContactPhone;
}
