package com.neusoft.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("meal_food")
public class MealFood {

    @TableId(type = IdType.AUTO)
    private Integer id;

    /** 餐ID */
    private Integer mealId;

    /** 食物ID */
    private Integer foodId;
}
