package com.lxc.usercenter.service;

import com.lxc.usercenter.mapper.UserMapper;
import com.lxc.usercenter.model.domain.User;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.StopWatch;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * @author mortal
 * @date 2024/4/17 17:49
 */
@SpringBootTest
public class InsertUsersTest {
	@Resource
	private UserMapper userMapper;
	@Resource
	private UserService userService;

	@Test
	public void testInsertUsers() {
		// Spring Framework 中的 StopWatch 类，用于测量代码执行时间。
		StopWatch stopWatch = new StopWatch();
		stopWatch.start();
		ArrayList<User> userList = new ArrayList<>();
		final int INSERT_NUM = 6000;
		for (int i = 0; i < INSERT_NUM; i++) {
			// 调用mapper接口插入
			User user = new User();
			user.setUsername("假数据lxc");
			user.setUserAccount("fakeLxc");
			user.setAvatarUrl("https://himg.bdimg.com/sys/portrait/item/public.1.f3c36e29.YM33Kex75LjO9Q6HbYDK0g?tt=1711855483217");
			user.setGender(0);
			user.setUserPassword("12345678");
			user.setPhone("12312312311");
			user.setEmail("234234234@qq.com");
			user.setUserStatus(0);
			user.setIsDelete(0);
			user.setUserRole(0);
			user.setPlanetCode("33222");
			user.setTags("[\"男\",\"C\",\"纯情小楚楠\"]");
//			userMapper.insert(user);
			userList.add(user);
		}
		userService.saveBatch(userList, 6000);

		stopWatch.stop();
		System.out.println(stopWatch.getTotalTimeMillis());
	}


	/**
	 * 并发（concurrent）批量插入用户
	 * 在 Java 编程中，"并发" 通常指的是在多个线程同时执行任务的能力。
	 * 在并发编程中，多个任务可能交替执行，每个任务都可能在一段时间内执行一部分，然后暂停，让其他任务执行
	 * 在并行编程中，多个任务可以真正同时执行，每个任务都可以在独立的处理器核心上运行，而不会相互干扰。
	 * CompletableFuture异步任务对象
	 * <p>
	 * 虽然只有一个异步任务，但仍然可以称之为并发编程（因为同一时间执行多个任务 --不需要同时）。这是因为这个异步任务的执行和 当前线程 是 并发 进行的，
	 * 也就是说，在异步任务执行的过程中，当前线程可以继续执行其他操作，而不需要等待异步任务的完成。。
	 */
	@Test
	public void doConcurrencyInsertUser() {
		StopWatch stopWatch = new StopWatch();
		stopWatch.start();
		final int INSERT_NUM = 1000;
		// 分十组
		int j = 0;
		//批量插入数据的大小
		int batchSize = 100;
		// 这个List集合 用于存储 CompletableFuture<Void> 对象，这些对象表示将来会完成的异步任务。
		List<CompletableFuture<Void>> futureList = new ArrayList<>();
		// i 要根据数据量和插入批量来计算需要循环的次数
		for (int i = 0; i < INSERT_NUM / batchSize; i++) {
			List<User> userList = new ArrayList<>();
			while (true) {
				j++;
				User user = new User();
				user.setUsername("lxc");
				user.setUserAccount("testlxc");
				user.setAvatarUrl("https://p6-sign.douyinpic.com/obj/douyin-user-image-file/9c8e829207b7322b38d83302c494098f?x-expires=1713394800&x-signature=%2BjyI6xkNDPTfG51KR76%2B0WLG3K8%3D&from=2064092626");
				user.setProfile("哥的帅你无需多言");
				user.setGender(0);
				user.setUserPassword("12345678");
				user.setPhone("123456");
				user.setEmail("789@qq.com");
				user.setUserStatus(0);
				user.setUserRole(0);
				user.setPlanetCode("931");
				user.setTags("[\"男\",\"C\",\"纯情小楚楠\"]");
				userList.add(user);
				// 当j == 10000 ，也就是 userList里面10000个user元素之后，执行下面
				if (j % batchSize == 0) {
					break;
				}
			}
			/**
			 * CompletableFuture.runAsync() 方法开启了一个新的线程，这使得任务可以在新的线程中异步执行，而不会阻塞当前线程。
			 * 开启了一个新的线程，我们才能达到异步，要不然一直是同步
			 */
			//异步执行
			// 创建了一个 CompletableFuture 异步任务的对象 future，并调用了 runAsync 方法来异步执行一个任务
			// 注意阿：我们这个 异步任务在for循环中，循环10次，所以我们会 开启10个线程
			CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
				// 这个例子中，任务是通过 Lambda 表达式定义的，会在一个新的线程中执行。
				// 这个任务的内容是调用 userService.saveBatch(userList, batchSize) 方法保存用户列表。
				System.out.println("ThreadName：" + Thread.currentThread().getName());
				// 100000条数据，分10组，每组一个线程，每个线程批量插入10000条数据
				userService.saveBatch(userList, batchSize);
			});
			// 将刚创建的 CompletableFuture 对象 future 添加到 futureList 中，以便后续跟踪任务的执行状态。
			futureList.add(future);
		}
		// 使用 CompletableFuture.allOf(...) 方法等待所有的 CompletableFuture 对象都完成。
		// allOf 方法接受一个 CompletableFuture 数组作为参数，并返回一个新的 CompletableFuture异步任务对象，
		// 它在所有参数 CompletableFuture 对象都完成时才会完成。
		// 通过调用 join 方法 等待 CompletableFuture 完成，主线程 会阻塞 在这里直到所有任务都执行完成。
		CompletableFuture.allOf(futureList.toArray(new CompletableFuture[]{})).join();

		stopWatch.stop();
		System.out.println(stopWatch.getLastTaskTimeMillis());

	}
}