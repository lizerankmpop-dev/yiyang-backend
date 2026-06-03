package com.neusoft.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 系统用户实体
 */
@Data
@TableName("user")
public class User {

    @TableId(type = IdType.AUTO)
    private Integer id;

    private String username;

    private String password;

    /** 昵称 */
    private String nickname;

    /** 性别：1-男 2-女 */
    private Integer sex;

    /** 邮箱 */
    private String email;

    /** 手机号 */
    private String phoneNumber;

    /** 角色：admin/nurse */
    private String role;

    /** 角色ID */
    private Integer roleId;

    /** 状态：1-启用 0-禁用 */
    private Integer status;
}
