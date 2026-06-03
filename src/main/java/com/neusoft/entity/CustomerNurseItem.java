package com.neusoft.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.math.BigDecimal;

@Data
@TableName("customer_nurse_item")
public class CustomerNurseItem {

    @TableId(type = IdType.AUTO)
    private Integer id;

    /** 客户ID */
    private Integer customerId;

    /** 护理项目ID */
    private Integer itemId;

    /** 护理项目名称 */
    private String itemName;

    /** 总次数 */
    private Integer totalCount;

    /** 已使用次数 */
    private Integer usedCount;

    /** 单价 */
    private BigDecimal price;

    /** 总价 */
    private BigDecimal totalPrice;

    /** 开始日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;

    /** 到期日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate expirationDate;

    /** 状态：1-正常 2-到期 3-欠费 */
    private Integer status;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /** 更新时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    /** 逻辑删除：0-未删除 1-已删除 */
    @TableLogic
    private Integer isDeleted;
    
    /** 客户姓名（非数据库字段） */
    @TableField(exist = false)
    private String customerName;
}
