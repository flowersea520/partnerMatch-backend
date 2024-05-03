package com.lxc.usercenter.job;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lxc.usercenter.mapper.UserMapper;
import com.lxc.usercenter.model.domain.User;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 在 job包下的 都是 定时任务类
 *
 * @author mortal
 * @date 2024/4/21 21:38
 */
@Component
@Slf4j
public class PreCacheJob {

	@Resource
	private UserMapper userMapper;
	@Resource
	private RedisTemplate redisTemplate;
	@Resource
	private RedissonClient redissonClient;
	// 重点用户列表（相当于一个白名单）
	private List<Long> mainUserList = Arrays.asList(1L);

	// 每天执行， 预热推荐用户
	// 在秒、分、小时、月、年等字段中，问号（？）和星号（*）是等价的，都表示匹配任意值。
	//但在天（日）和星期字段中，问号（？）表示不指定值，而星号（*）表示匹配任意值。
	@Scheduled(cron = "0 10 19 * * *")  // 秒分时日周月  --
	public void doCacheRecommendUser() {

		RLock lock = redissonClient.getLock("partnerMatch:preCachejob:doCache:lock");
		try {
			// 第一个参数 0 是等待获取锁的最长时间（单位为毫秒），
			// 如果传入 0，表示立即尝试获取锁而不等待。如果传入一个正数，则表示在指定的时间内等待获取锁。
			// 第二个参数：锁的最长持有时间，即获取锁后，允许的最长持有时间。
			// 在指定的时间内未手动释放锁，则系统将自动释放锁。
			if (lock.tryLock(0, 30000L, TimeUnit.MILLISECONDS)) {
				System.out.println("getLock: " + Thread.currentThread().getId());

				for (Long userId : mainUserList) {
					QueryWrapper<User> queryWrapper = new QueryWrapper<>();
					// limit在mysql中就是为了分页查询，所以 mybatisplus中的的page方法对应 limit的sql（有两个参数，分别是偏移量（当前行的索引）和行数
					Page<User> userPage = userMapper.selectPage(new Page<>(1, 20), queryWrapper);
					// String.format() 方法用于将字符串模板中的 %s 占位符替换为实际的用户ID，从而得到最终的Redis键。
					String redisKey = String.format("partnerMatch:userService:recommend:%s", userId);
					// 将ValueOperations理解为redis操作value的一个对象，可以指定泛型）
					ValueOperations<String, Object> valueOperations = redisTemplate.opsForValue();
					// 写缓存, 并设置redis的过期时间（因为redis的内存不能无限增加）
					try {
						// TimeUnit是Java中时间单位的一个枚举类
						valueOperations.set(redisKey, userPage, 30000, TimeUnit.MILLISECONDS);
					} catch (Exception e) {
						log.error("redis set key error", e);
					}
				}
			}
		} catch (InterruptedException e) {
			log.error("doCacheRecommendUser error");
		} finally {
			// 无论如何，都会释放自己的锁
			if (lock.isHeldByCurrentThread()) {
				System.out.println("unLock: " + Thread.currentThread().getId());
				// isHeldByCurrentThread() 是用来检查当前线程是否持有该锁的方法。
				// 如果当前线程持有该锁，则通过 unlock() 方法释放锁。
				lock.unlock();
			}
		}


	}

}
