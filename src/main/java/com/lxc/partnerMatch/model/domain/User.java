package com.lxc.partnerMatch.model.domain;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

/**
 * 用户实体
 *
 *  *author lxc
 */
@TableName(value = "user")
@Data
public class User implements Serializable {
    private static final long serialVersionUID = 7693137914608792591L;

    /**
     * id
     */
    @TableId(type = IdType.AUTO)
    private long id;

    /**
     * 用户昵称
     */
    private String username;

    /**
     * 账号
     */
    private String userAccount;

    /**
     * 用户头像
     */
    private String avatarUrl;

    /**
     * 性别
     */
    private Integer gender;

    /**
     * 密码
     */
    private String userPassword;

    /**
     * 电话
     */
    private String phone;

    /**
     *   个人简介
     */
    private String profile;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 状态 0 - 正常
     */
    private Integer userStatus;

    /**
     * 创建时间
     */
    // 人话：返回给前端需要的格式
    @JsonFormat(pattern = "yyyy-MM-dd", timezone="GMT+8") // 用于指定后端序列化时的日期格式，
    // 人话：返回给后端（数据库，需要的格式）
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss") // 用于指定后端反序列化时的日期格式。
    private Date createTime;

    /**
     *
     */
    // 人话：返回给前端需要的格式
    @JsonFormat(pattern = "yyyy-MM-dd", timezone="GMT+8") // 用于指定后端序列化时的日期格式，
    // 人话：返回给后端（数据库，需要的格式）
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss") // 用于指定后端反序列化时的日期格式。
    private Date updateTime;

    /**
     * 是否删除
     */
    @TableLogic
    private Integer isDelete;

    /**
     * 用户角色 0 - 普通用户 1 - 管理员
     */
    private Integer userRole;

    /**
     * 星球编号
     */
    private String planetCode;

    /**
     * 用户标签  JSON 列表
     */
    private String tags;


}
