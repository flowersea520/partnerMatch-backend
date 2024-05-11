package com.lxc.partnerMatch.common;


/**
 * 错误码
 *
 这个枚举类是一种常见的用于 定义 错误码和错误消息的方式。
 在很多项目中，特别是基于 RESTful API 的项目中，为了规范错误信息的返回和处理，会定义 一套 统一的 错误码 和 对应的错误消息。
 定义错误码，尽量让其有语义化，例如404，可以定义为  40400
 * author lxc
 */
public enum ErrorCode {
	/**
	 * 出于安全考虑而不直接返回系统的内部错误信息给用户。
	 * 直接返回系统的内部错误信息可能会给潜在的攻击者提供有利的信息，例如系统的结构、配置或漏洞信息，从而增加系统面临的风险。
	 * 通过使用自定义的 错误码 和错误消息，可以屏蔽系统的内部细节，
	 * 提供给用户一个更加友好和安全的错误信息。这样做可以保护系统的安全性，
	 * 同时也有利于用户理解和处理错误，提高了系统的安全性和可用性。
	 */
	SUCCESS(0, "ok", ""),
	PARAMS_ERROR(40000, "请求参数错误", ""),
	NULL_ERROR(40001, "请求数据为空", ""),
	NOT_LOGIN(40100, "未登录", ""),
	NO_AUTH(40101, "无权限", ""),
// FORBIDDEN 是指禁止访问的状态。forbid的过去分词，forbidden：禁止
	FORBIDDEN(40301, "禁止访问", ""),
	SYSTEM_ERROR(50000, "系统内部异常", "");

	private final int code;

	/**
	 * 状态码信息
	 */
	private final String message;

	/**
	 * 状态码描述（详情）
	 */
	private final String description;

	ErrorCode(int code, String message, String description) {
		this.code = code;
		this.message = message;
		this.description = description;
	}

	public int getCode() {
		return code;
	}

	public String getMessage() {
		return message;
	}

	// https://t.zsxq.com/0emozsIJh

	public String getDescription() {
		return description;
	}
}
