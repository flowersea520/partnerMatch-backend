package com.lxc.usercenter.service;

import com.baomidou.mybatisplus.core.conditions.AbstractWrapper;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.lxc.usercenter.mapper.UserMapper;
import com.lxc.usercenter.model.domain.User;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;

import com.lxc.usercenter.contant.UserConstant;

import static com.lxc.usercenter.contant.UserConstant.SALT;

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
