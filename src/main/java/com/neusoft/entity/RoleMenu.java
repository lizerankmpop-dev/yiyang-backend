package com.neusoft.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("role_menu")
public class RoleMenu {

    @TableId(type = IdType.AUTO)
    private Integer id;

    /** 角色ID */
    private Integer roleId;

    /** 菜单ID */
    private Integer menuId;
}
