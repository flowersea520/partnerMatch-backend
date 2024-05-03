package com.lxc.usercenter.model.enums;

/**
 * 当你在代码中需要表示一组固定的常量时，枚举类是一个很好的选择。
 * 枚举类可以帮助你更清晰地表达代码的意图，并且可以提高代码的可读性和可维护性。
 *
 * @author mortal
 * @date 2024/4/27 19:11
 */


/**
 *  队伍状态的枚举
 */
public enum TeamStatusEnum {
	/**
	 * 定义的 PUBLIC、PRIVATE 和 SECRET 就是枚举的对象，它们属于枚举类的实例
	 * 也是枚举 常量
	 * 可以使用 枚举类 的名称加上 枚举常量 的名称来访问。
	 */
	// 这是三个枚举对象
	PUBLIC(0, "公开"),
	PRIVATE(1, "私有"),
	SECRET(2, "加密");

	private int value;
	private String text;

	/**
	 * （就根据队伍的状态码，然后找到对应的枚举值，判断是否是加密或者私有）
	 * @param value
	 * @return
	 */
// 根据给定的整数值 value 在 TeamStatusEnum 枚举类中查找相应的枚举常量。
	public static TeamStatusEnum getEnumByValue(Integer value) {
		if (value == null) {
			return null;
		}
		//values() 方法是枚举类中的一个静态方法，它返回一个包含枚举类所有枚举常量的数组
		// 返回的就是所有的枚举常量（也就是枚举对象）
		TeamStatusEnum[] values = TeamStatusEnum.values();
		// teamStatusEnum这个就是遍历出来的枚举常量
		for (TeamStatusEnum teamStatusEnum : values) {
			if (teamStatusEnum.value == value) {
				return teamStatusEnum;
			}
		}
		return null;
	}

	TeamStatusEnum(int value, String text) {
		this.value = value;
		this.text = text;
	}

	public int getValue() {
		return value;
	}

	public void setValue(int value) {
		this.value = value;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}
}
