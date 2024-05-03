package com.lxc.usercenter.model.request;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 *  队伍更新的DTO
 * @author mortal
 * @date 2024/4/28 14:57
 */
@Data
public class TeamUpdateRequest implements Serializable {


	private static final long serialVersionUID = 4120482573640681511L;
	/**
	 * id  (根据id来更新队伍）
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
	 * 过期时间
	 */
	private Date expireTime;



	/**
	 *  0 - 公开， 1 - 私有，2 - 加密
	 */
	private Integer status;

	/**
	 * 密码
	 */
	private String password;

}
