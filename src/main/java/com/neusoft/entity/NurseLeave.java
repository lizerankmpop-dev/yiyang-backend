package com.neusoft.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 护工请假实体
 */
@Data
@TableName("nurse_leave")
public class NurseLeave {

    @TableId(type = IdType.AUTO)
    private Integer id;

    /** 护工ID */
    private Integer nurseId;

    /** 护工姓名 */
    private String nurseName;

    /** 请假类型：年假、病假、事假、婚假、产假、丧假、其他 */
    private String leaveType;

    /** 开始日期 */
    private LocalDate startDate;

    /** 结束日期 */
    private LocalDate endDate;

    /** 请假天数 */
    private Integer totalDays;

    /** 请假原因 */
    private String reason;

    /**
     * 状态：0-待审批 1-已通过 2-已拒绝 3-已销假
     */
    private Integer status;

    /** 申请人ID */
    private Integer applicantId;

    /** 申请人姓名 */
    private String applicantName;

    /** 审批人ID */
    private Integer approverId;

    /** 审批人姓名 */
    private String approverName;

    /** 审批备注 */
    private String approveRemark;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;

    /** 逻辑删除：0-未删除 1-已删除 */
    @TableLogic
    private Integer isDeleted;
}
