package com.lxc.usercenter.service;

import com.lxc.usercenter.model.domain.User;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import javax.annotation.Resource;

/**
 * @author mortal
 * @date 2024/4/19 11:08
 */
@SpringBootTest
public class RedisTest {
	/**
	 * 当我们将对象作为键存储到Redis中时，对象会首先通过键的序列化器转换为字节流或字符串，
	 * 然后才会被存储到Redis中。
	 * 反之，当我们从Redis中检索键时，Redis会返回键的字节流或字符串形式，
	 * 然后RedisTemplate会使用相同的键的序列化器将其转换回Java对象。
	 */
	@Resource
	private RedisTemplate<String, String> redisTemplate;
//在Redis中说的"序列化"指的就是将Java对象转换为Redis的字符串（redis的键通常都是字符串）
// 字节流（如果要存储的话，会将字符串变成字节流在redis中存储）
	@Test
	void test() {
		// ValueOperations 是 RedisTemplate 提供的用于操作 Redis 字符串类型数据的接口。
		// 它主要用于操作 Redis 中的单个字符串值，因此可以用来操作字符串类型的数据，包括字符串和数字。
		// 但是存储的数字或者实体类，使用这个ValueOperations接口的时候，存储在redis中都会变为 字符串
		ValueOperations valueOperations = redisTemplate.opsForValue();
		// 给redis中增加键值对
		valueOperations.set("lxcString", "dog");
		valueOperations.set("lxc1Int", 1);
		valueOperations.set("lxc2Double", 2.0);
		User user = new User();
		user.setId(1L);
		user.setUsername("lxc");
		valueOperations.set("lxcUser", user);

		/**
		 *  使用ValueOperations的set方法，他的value只能存储字符串，存储别的数字类型或者实体类都会变成字符串的
		 */
		Object lxc = valueOperations.get("lxcString");
		Assertions.assertTrue("dog".equals(lxc));

		Object lxc1 = valueOperations.get("lxc1Int");
//		存储别的数字类型或者实体类都会变成字符串的, 所以我们这里要强转成 对应的断言类型（注意引用类型要转换成对应的包装类）
		Assertions.assertTrue(1 == (Integer) lxc1);
		Object lxc2 = valueOperations.get("lxc2Double");
		Assertions.assertTrue(2.0 == (Double) lxc2);

		Object lxcUser = valueOperations.get("lxcUser");
		Assertions.assertTrue(lxcUser.equals(user));
		System.out.println(lxcUser);


	}

}
