package com.neusoft.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableLogic;
import lombok.Data;

import java.util.List;

@Data
@TableName("nursing_level")
public class NursingLevel {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private String levelName;
    private String description;

    /** 状态：1-启用 2-停用 */
    private Integer levelStatus;

    /** 逻辑删除：0-未删除 1-已删除 */
    @TableLogic
    private Integer isDeleted;

    @TableField(exist = false)
    private List<Integer> itemIds;
}
