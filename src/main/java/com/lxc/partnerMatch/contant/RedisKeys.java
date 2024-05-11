package com.lxc.partnerMatch.contant;

/**
 *  redis 的key常量类 ：用来获取对应的value -- 有时候这个value通常存放的实体类，在redis中以字符串展示
 * @author mortal
 * @date 2024/5/10 17:31
 */
public class RedisKeys {
	//    String.format使用这个方法，将%s的用户id补上（如果是用户抢锁，就补上用户id，作为业务区分）
	public static final String LOGIN_USERID_LOCK = "partnerMatch:userService:recommend:%s";
}
