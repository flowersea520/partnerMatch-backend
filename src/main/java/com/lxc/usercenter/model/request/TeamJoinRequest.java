package com.lxc.usercenter.model.request;

import lombok.Data;

import java.io.Serializable;

/**
 *  加入队伍的请求 dto  （DTO就是 根据业务添加对应的dto的目的，
 *  就是可以不用将数据库的所有实体属性列出来，我们可以自定义一些属性（封装成DTO，然后选择性的让其修改）
 *   例如：我们加入队伍，就需要用到 teamId和密码属性就可以了
 * @author mortal
 * @date 2024/4/28 14:57
 */
@Data
public class TeamJoinRequest implements Serializable {

	private static final long serialVersionUID = -8520488191121863719L;
	/**
	 * 队伍id
	 */
	private Long teamId;

	/**
	 * 密码
	 */
	private String password;

}
