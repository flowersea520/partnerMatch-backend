package com.lxc.partnerMatch.once;

import com.lxc.partnerMatch.mapper.UserMapper;
import com.lxc.partnerMatch.model.domain.User;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

import javax.annotation.Resource;

/**
 * @author mortal
 * @date 2024/4/17 17:10
 */
@Component
public class InsertUsers {

	@Resource
	private UserMapper userMapper;

	/**
	 *  我们使用 Java程序的方式 插入 1000条数据
	 */
	// 表示这个任务会每隔 5 秒执行一次。任务执行完成后，会等待 5 秒后再次执行
//	@Scheduled(initialDelay = 5000, fixedRate = Long.MAX_VALUE)
	public void doInsertUsers() {
		// Spring Framework 中的 StopWatch 类，用于测量代码执行时间。
		StopWatch stopWatch = new StopWatch();
		stopWatch.start();
		final int INSERT_NUM = 1000;
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
			userMapper.insert(user);
		}
		stopWatch.stop();
		System.out.println(stopWatch.getTotalTimeMillis());
	}



}
