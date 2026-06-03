package com.neusoft.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.math.BigDecimal;

@Data
@TableName("backdown")
public class Backdown {

    @TableId(type = IdType.AUTO)
    private Integer id;

    /** 客户ID */
    private Integer customerId;

    /** 客户姓名 */
    private String customerName;

    /** 申请人ID */
    private Integer applicantId;

    /** 申请人姓名 */
    private String applicantName;

    /** 申请日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate applyDate;

    /** 退住日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate checkOutDate;

    /** 退住原因 */
    private String reason;

    /** 退住类型：1-正常 2-死亡 */
    private Integer backdownType;

    /** 状态：0-待审核 1-通过 2-驳回 */
    private Integer status;

    /** 审核人ID */
    private Integer auditorId;

    /** 审核人姓名 */
    private String auditorName;

    /** 审核时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime auditTime;

    /** 审核备注 */
    private String auditRemark;

    /** 结算金额 */
    private BigDecimal settlementAmount;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /** 更新时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    /** 逻辑删除：0-未删除 1-已删除 */
    @TableLogic
    private Integer isDeleted;
}
