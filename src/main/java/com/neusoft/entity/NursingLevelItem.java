package com.neusoft.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 护理级别-护理项目关联实体
 * 修复：
 * 1. 主键改为数据库自增（IdType.AUTO），不再需要手动计算 nextId
 * 2. 统一使用 Lombok @Data
 */
@Data
@TableName("nursing_level_item")
public class NursingLevelItem {

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    private Integer levelId;

    private Integer itemId;
}
