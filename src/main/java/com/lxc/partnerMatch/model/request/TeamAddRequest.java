package com.lxc.partnerMatch.model.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

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
	// 人话：返回给前端需要的格式
	@JsonFormat(pattern = "yyyy-MM-dd", timezone="GMT+8") // 用于指定后端序列化时的日期格式，
	// 人话：返回给后端（数据库，需要的格式）
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
	 * 密码
	 */
	private String password;

}
