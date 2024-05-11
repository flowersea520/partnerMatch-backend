package com.lxc.partnerMatch.service;


import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.redisson.api.RList;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

/**
 * @author mortal
 * @date 2024/4/23 16:25
 */
@SpringBootTest
@Slf4j
public class RedissonTest {

	@Resource
	private RedissonClient redissonClient;

	@Test
	public void testRession() {
		// value对应的是list，但是 key的名字，我们自己指定
		// 使用 Redisson 客户端库创建一个 Redis List 数据结构，然后指定了这个列表list 的 key
		RList<Object> rList = redissonClient.getList("test-list");
		rList.add("lxc1");
		System.out.println("rList: " + rList.get(0));
		rList.remove(0);
		System.out.println("删除成功");

	}

	@Test
	public void testWatchDog() {
		RLock lock = redissonClient.getLock("partnerMatch:preCachejob:doCache:lock");
		try {
			// 在 Redisson 中，如果你在获取锁时将 leaseTime 参数设置为 -1，表示锁的持有时间为永久，不会自动释放。
			// 这意味着锁将一直保持直到显式地释放它，或者直到锁的持有者断开连接或发生其他异常情况。
			if (lock.tryLock(0, -1, TimeUnit.MILLISECONDS)) {
				Thread.sleep(30000);
				System.out.println("getLock: " + Thread.currentThread().getId());

			}
		} catch (InterruptedException e) {
			System.out.println(e.getMessage());
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
