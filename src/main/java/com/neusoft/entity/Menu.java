package com.neusoft.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("menu")
public class Menu {

    @TableId(type = IdType.AUTO)
    private Integer id;

    /** 父菜单ID */
    private Integer parentId;

    /** 菜单名称 */
    private String menuName;

    /** 菜单类型：1-目录 2-菜单 3-按钮 */
    private Integer menuType;

    /** 路由路径 */
    private String path;

    /** 组件路径 */
    private String component;

    /** 图标 */
    private String icon;

    /** 权限标识 */
    private String permission;

    /** 排序 */
    private Integer sortOrder;

    /** 是否可见：1-是 0-否 */
    private Integer visible;

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
