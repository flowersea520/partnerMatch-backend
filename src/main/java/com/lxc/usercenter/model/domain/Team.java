package com.lxc.usercenter.model.domain;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

/**
 * 队伍表
 * @TableName team
 */
@TableName(value ="team") // 相当于这个team实体类对应着team数据表，表中每行数据对应着一个team实体
@Data
public class Team implements Serializable {
    /**
     * id
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     *  队伍名称
     */
    private String name;

    /**
     * 描述 
     */
    private String description;

    /**
     *  队伍允许的 最大人数
     */
    private Integer maxNum;

    /**
     * 过期时间 
     */
    private Date expireTime;

    /**
     * 用户id
     */
    private Long userId;

    /**
     *  0 - 公开， 1 - 私有，2 - 加密
     *  （就根据队伍的状态码，然后找到对应的枚举值，判断是否是加密或者私有）
     */
    private Integer status;

    /**
     * 密码
     */
    private String password;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 
     */
    private Date updateTime;

    /**
     * 是否删除
     */
    @TableLogic
    private Integer isDelete;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Team team = (Team) o;
        return Objects.equals(id, team.id) && Objects.equals(name, team.name) && Objects.equals(description, team.description) && Objects.equals(maxNum, team.maxNum) && Objects.equals(expireTime, team.expireTime) && Objects.equals(userId, team.userId) && Objects.equals(status, team.status) && Objects.equals(password, team.password) && Objects.equals(createTime, team.createTime) && Objects.equals(updateTime, team.updateTime) && Objects.equals(isDelete, team.isDelete);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, description, maxNum, expireTime, userId, status, password, createTime, updateTime, isDelete);
    }
}