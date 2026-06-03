package com.neusoft.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableLogic;
import lombok.Data;

@Data
@TableName("nurse_item")
public class NurseItem {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private String itemName;
    private String category;
    private Integer price;
    private String unit;
    private String description;

    /** 编号 */
    private String serialNumber;

    /** 执行周期 */
    private String executionCycle;

    /** 执行次数 */
    private String executionTimes;

    /** 状态：1-启用 2-停用 */
    private Integer status;

    /** 逻辑删除：0-未删除 1-已删除 */
    @TableLogic
    private Integer isDeleted;
}
