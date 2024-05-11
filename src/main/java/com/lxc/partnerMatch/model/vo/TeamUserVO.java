package com.lxc.partnerMatch.model.vo;


import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;


/**
 * VO的作用：  ○ VO 是用于呈现视图（View）的对象。它通常用于控制器层（Controller）向 前端 页面传递数据。
 *   ○ VO 的设计目的 是为了 将 后端数据 以适合 前端 展示的 形式 进行封装，
 *   使得前端页面可以直接使用这些数据，而不需要再对数据进行额外的处理。
 * @author mortal
 * @date 2024/4/28 16:46
 */

/**
 * 队伍和用户信息的封装类（就是 team实体类的 脱敏版）
 */
@Data
public class TeamUserVO implements Serializable {


	private static final long serialVersionUID = 4322893961349920905L;
	/**
	 * id
	 */

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
	 * 最大人数
	 */
	private Integer maxNum;

	/**
	 * 过期时间
	 */
	@JsonFormat(pattern = "yyyy-MM-dd", timezone="GMT+8") // 用于指定后端序列化时的日期格式，
	@DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss") // 用于指定后端反序列化时的日期格式。
	private Date expireTime;

	/**
	 * 用户id
	 */
	private Long userId;

	/**
	 *  0 - 公开， 1 - 私有，2 - 加密
	 */
	private Integer status;

	/**
	 * 创建时间
	 */
	// 人话：返回给前端需要的格式
	@JsonFormat(pattern = "yyyy-MM-dd", timezone="GMT+8") // 用于指定后端序列化时的日期格式，
	// 人话：返回给后端（数据库，需要的格式）
	@DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss") // 用于指定后端反序列化时的日期格式。
	private Date createTime;

	/**
	 * 更新时间
	 */
	// 人话：返回给前端需要的格式
	@JsonFormat(pattern = "yyyy-MM-dd", timezone="GMT+8") // 用于指定后端序列化时的日期格式，
	// 人话：返回给后端（数据库，需要的格式）
	@DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss") // 用于指定后端反序列化时的日期格式。
	private Date updateTime;

	/**
	 *  创建人用户信息
	 */
	private UserVO createUser;

	/**
	 *  已加入的用户数
	 */
	private long hasJoinNum;

	/**
	 *  是否已加入队伍
	 *  boolean它的默认值就是 false。
	 */
	private boolean hasJoin;


}
