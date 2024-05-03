package com.lxc.usercenter.model.request;

import lombok.Data;

import java.util.Date;

/**
 * @author mortal
 * @date 2024/4/28 14:57
 */
@Data
public class TeamAddRequest {
//	/**
//	 * id 不用传，mysql主键自增
//	 */
//	private Long id;

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
	 * 密码
	 */
	private String password;

}
