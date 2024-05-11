package com.lxc.partnerMatch.service;

import com.baomidou.mybatisplus.core.conditions.AbstractWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.lxc.partnerMatch.mapper.UserMapper;
import com.lxc.partnerMatch.model.domain.User;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;

import static com.lxc.partnerMatch.contant.UserConstant.SALT;

/**
 * @author mortal
 * @date 2024/4/21 17:05
 */
@SpringBootTest
public class UpdateUserPas {
	@Resource
	private UserMapper userMapper;

	@Test
	void test() {
		User user = new User();
		for (int i = 2; i <= 7; i++) {
			String userPassword = "12345678";
			String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
			user.setUserPassword(encryptPassword);
			AbstractWrapper queryWrapper = new QueryWrapper<>();
			queryWrapper.eq("id", i);
			int update = userMapper.update(user, queryWrapper);
			Assertions.assertTrue(update>0);
		}
	}
}
