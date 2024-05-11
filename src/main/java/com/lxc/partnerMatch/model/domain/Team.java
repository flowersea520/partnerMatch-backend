package com.lxc.partnerMatch.model.domain;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

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
     * 例如默认传：2024/05/06的日期，Jackson 数据绑定库无法直接将这个字符串解析为日期对象，因为它不符合 Java 默认的日期格式。
     * 使用 @JsonFormat 注解来指定日期格式
     * 我们在 expireTime 字段上同时使用了 @JsonFormat 和 @DateTimeFormat 注解。
     * @JsonFormat 注解用于指定后端序列化时的日期格式，
     * 而 @DateTimeFormat 注解用于指定后端反序列化时的日期格式。
     * 这样，在接收前端传递的日期时，Spring MVC 会自动将字符串转换为 Date 类型。
     */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone="GMT+8") // 用于指定后端序列化时的日期格式，
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss") // 用于指定后端反序列化时的日期格式。
    private Date expireTime;

    /**
     * 用户id（队伍创建的用户id）
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