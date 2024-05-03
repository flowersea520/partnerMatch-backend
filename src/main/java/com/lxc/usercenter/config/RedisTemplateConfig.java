package com.lxc.usercenter.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * redis中的key都是字符串类型的
 * “key的序列化器”是一个能够将应用程序中的key数据类型（例如字符串类型）转换为Redis能够理解的字节流的机制。
 * 对于大多数应用而言，这个机制  处理的是   字符串类型的key ，但也可以扩展到其他数据类型。
 * 当我们需要将一个字符串作为key存储时，我们需要先将这个 字符串 转换为字节流，然后这个字节流才会被Redis存储。
 *
 * @author mortal
 * @date 2024/4/21 15:34
 */
@Configuration
public class RedisTemplateConfig {
	@Bean
	public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
		RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
		redisTemplate.setConnectionFactory(redisConnectionFactory);
		// 指定 key的序列化器
		//  键的序列化器决定了在将键存储到 Redis 中或从 Redis 中查找对应的键时，
		//  如何将Java 对象转换为 Redis 可接受的形式。
		// 当我们从Redis中查找对应的键时，Redis会返回键的字节流或字符串形式，
		// 然后RedisTemplate会使用相同的键的序列化器将其转换回Java对象。
		// 这里我们指定了序列化器后，发现存储在redis服务器中的key，没有乱码了，因为这个序列化器，是以utf-8存储的
		redisTemplate.setKeySerializer(new StringRedisSerializer());
		return redisTemplate;
	}
}
