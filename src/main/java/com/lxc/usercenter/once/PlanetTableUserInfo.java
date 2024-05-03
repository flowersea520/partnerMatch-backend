package com.lxc.usercenter.once;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;


/**
 *
 * 星球表格用户信息
 *
 * @author mortal
 * @date 2024/4/13 17:06
 */
@Data
@EqualsAndHashCode
public class PlanetTableUserInfo {

	/**
	 * @ExcelProperty: 这是 EasyExcel 提供的一个注解，用于标识 Java 类中的字段与 Excel 文件中的列之间的映射关系。
	 */
	@ExcelProperty("成员编号")
	private String planetCode; // 对应这成员编号那一列的数据映射

	/**
	 * 用户昵称
	 */
	@ExcelProperty("成员昵称")
	private String username;

}
