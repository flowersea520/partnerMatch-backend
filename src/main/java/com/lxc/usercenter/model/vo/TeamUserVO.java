package com.lxc.usercenter.model.vo;


import lombok.Data;

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
	private Date createTime;

	/**
	 * 更新时间
	 */
	private Date updateTime;

	/**
	 *  创建人用户信息
	 */
	UserVO createUser;


}
