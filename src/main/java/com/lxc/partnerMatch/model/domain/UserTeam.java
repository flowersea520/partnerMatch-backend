package com.lxc.partnerMatch.model.domain;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

/**
 * 用户队伍关系表
 * @TableName user_team
 */
@TableName(value ="user_team")
@Data
public class UserTeam implements Serializable {
    /**
     * id
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     *  用户id
     */
    private Long userId;

    /**
     *  队伍Id 
     */
    private Long teamId;

    /**
     * 加入时间 
     */
    private Date joinTime;

    /**
     * 创建时间
     */
    // 人话：返回给前端需要的格式
    @JsonFormat(pattern = "yyyy-MM-dd", timezone="GMT+8") // 用于指定后端序列化时的日期格式，
    // 人话：返回给后端（数据库，需要的格式）
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss") // 用于指定后端反序列化时的日期格式。
    private Date createTime;

    /**
     *  更新时间
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
}