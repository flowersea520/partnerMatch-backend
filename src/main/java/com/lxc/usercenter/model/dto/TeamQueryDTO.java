package com.lxc.usercenter.model.dto;

import com.lxc.usercenter.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * 专门用来 队伍查询的 （只要用到了队伍查询，就使用这个dto）
 * 封装类：使用的原因
 * 1. 可能有些字段需要隐藏，不能返回给前端
 * 2. 有些字段的某些方法是不关心的
 * @author mortal
 * @date 2024/4/26 18:00
 */
// @EqualsAndHashCode(callSuper = true) 是 Lombok 提供的注解，
// 用于为类自动生成 equals() 和 hashCode() 方法，并调用父类的对应方法
@EqualsAndHashCode(callSuper = true)
@Data
// 通过让 TeamQuery 类继承自 PageRequest 类，
// TeamQuery 类就可以访问到 PageRequest 类中被 protected 修饰的成员变量 pageNum 和 pageSize。
public class TeamQueryDTO extends PageRequest {
	/**
	 * id
	 */
	private Long id;

	/**
	 *  搜索关键字（同时对队伍名称和描述详情搜索）
	 */
	private String searchText;

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
	 * 用户id
	 */
	private Long userId;

	/**
	 *  0 - 公开， 1 - 私有，2 - 加密
	 */
	private Integer status;

}
