package com.lxc.usercenter.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.lxc.usercenter.common.ErrorCode;
import com.lxc.usercenter.common.ResultUtils;
import com.lxc.usercenter.exception.BusinessException;
import com.lxc.usercenter.model.domain.User;
import com.lxc.usercenter.service.UserService;
import com.lxc.usercenter.mapper.UserMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.lxc.usercenter.contant.UserConstant.*;

/**
 * 用户服务实现类
 * *author lxc
 */
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
		implements UserService {

	@Resource
	private UserMapper userMapper;
	@Resource
	private RedisTemplate redisTemplate;




	/**
	 * 用户注册
	 *
	 * @param userAccount   用户账户
	 * @param userPassword  用户密码
	 * @param checkPassword 校验密码
	 * @param planetCode    星球编号
	 * @return 新用户 id
	 */
	@Override
	public long userRegister(String userAccount, String userPassword, String checkPassword, String planetCode) {
		// 1. 校验
		if (StringUtils.isAnyBlank(userAccount, userPassword, checkPassword, planetCode)) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
		}
		if (userAccount.length() < 4) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户账号过短");
		}
		if (userPassword.length() < 8 || checkPassword.length() < 8) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户密码过短");
		}
		if (planetCode.length() > 5) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR, "星球编号过长");
		}
		// 账户不能包含特殊字符
		String validPattern = "[`~!@#$%^&*()+=|{}':;',\\\\[\\\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]";
		Matcher matcher = Pattern.compile(validPattern).matcher(userAccount);
		if (matcher.find()) {
			return -1;
		}
		// 密码和校验密码相同
		if (!userPassword.equals(checkPassword)) {
			return -1;
		}
		// 账户不能重复
		QueryWrapper<User> queryWrapper = new QueryWrapper<>();
		queryWrapper.eq("userAccount", userAccount);
		long count = userMapper.selectCount(queryWrapper);
		if (count > 0) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号重复");
		}
		// 星球编号不能重复
		queryWrapper = new QueryWrapper<>();
		queryWrapper.eq("planetCode", planetCode);
		count = userMapper.selectCount(queryWrapper);
		if (count > 0) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR, "编号重复");
		}
		// 2. 加密
		String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
		// 3. 插入数据
		User user = new User();
		user.setUserAccount(userAccount);
		user.setUserPassword(encryptPassword);
		user.setPlanetCode(planetCode);
		boolean saveResult = this.save(user);
		if (!saveResult) {
			return -1;
		}
		return user.getId();
	}

	/**
	 * 用户登录
	 *
	 * @param userAccount  用户账户
	 * @param userPassword 用户密码
	 * @param request
	 * @return 脱敏后的用户信息
	 */
	@Override
	public User userLogin(String userAccount, String userPassword, HttpServletRequest request) {
		// 1. 校验
		if (StringUtils.isAnyBlank(userAccount, userPassword)) {
			throw new BusinessException(ErrorCode.NULL_ERROR, "账户密码为空");
		}
		if (userAccount.length() < 4) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户账号长度小于4位");
		}
		if (userPassword.length() < 8) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户密码长度小于8位");
		}
		// 账户不能包含特殊字符
		String validPattern = "[`~!@#$%^&*()+=|{}':;',\\\\[\\\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]";
		Matcher matcher = Pattern.compile(validPattern).matcher(userAccount);
		if (matcher.find()) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户账户包含特殊字符");
		}
		// 2. 加密  （因为在用户注册的时候我们把用户的密码加密了放在数据库，所以我们登录的时候也要加密，才能和数据库匹配密码，
		//  			然后找到其对象）
		String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
		// 查询用户是否存在
		QueryWrapper<User> queryWrapper = new QueryWrapper<>();
		queryWrapper.eq("userAccount", userAccount);
		queryWrapper.eq("userPassword", encryptPassword);
		User user = userMapper.selectOne(queryWrapper);
		// 用户不存在
		if (user == null) {
			log.info("user login failed, userAccount cannot match userPassword");
			throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户不存在");
		}
		// 3. 用户脱敏
		User safetyUser = getSafetyUser(user);
		// 4. 记录用户的登录态
		// 给用户的会话(session)设置了一个标识。
		// 具体来说，它将一个名为 USER_LOGIN_STATE 的属性与 safetyUser 对象相关联，
		// 这样在用户的会话期间，可以通过这个属性来获取和操作 safetyUser 对象。
		/**
		 * request.getSession()就是不存在，创建会话， 存在，取存在的会话
		 * 设置会话属性时，如果会话是新创建的，Servlet 容器或 Spring Boot 会自动创建一个会话 Cookie 并将其附加到响应中。
		 * 这个 Cookie 的名字通常是 JSESSIONID，用于唯一标识用户的会话。
		 */
		request.getSession().setAttribute(USER_LOGIN_STATE, safetyUser);
		return safetyUser;
	}

	/**
	 * 用户脱敏
	 *
	 * @param originUser
	 * @return
	 */
	@Override
	public User getSafetyUser(User originUser) {
		if (originUser == null) {
			return null;
		}
		User safetyUser = new User();
		safetyUser.setId(originUser.getId());
		safetyUser.setUsername(originUser.getUsername());
		safetyUser.setUserAccount(originUser.getUserAccount());
		safetyUser.setAvatarUrl(originUser.getAvatarUrl());
		safetyUser.setGender(originUser.getGender());
		safetyUser.setPhone(originUser.getPhone());
		safetyUser.setEmail(originUser.getEmail());
		safetyUser.setPlanetCode(originUser.getPlanetCode());
		safetyUser.setUserRole(originUser.getUserRole());
		safetyUser.setUserStatus(originUser.getUserStatus());
		safetyUser.setCreateTime(originUser.getCreateTime());
		safetyUser.setTags(originUser.getTags());
		safetyUser.setProfile(originUser.getProfile());
		return safetyUser;
	}

	/**
	 * 用户注销
	 *
	 * @param request
	 */
	@Override
	public int userLogout(HttpServletRequest request) {
		// 移除登录态
		request.getSession().removeAttribute(USER_LOGIN_STATE);
		return 1;
	}

	/**
	 * 根据标签搜索用户（内存过滤 版）
	 * （只要数据库中都包含了，传过来的标签列表，就符合要求）
	 * 例如传过来两个标签 java c++；   而对应的数据库中user用户对象的 标签是 java python c++；那符合要求
	 * （因为都包含了传过来的标签，数据库中的标签和传过来的不匹配，哪怕少一个都不行）
	 * 例如传过来 java js，   那就不符合，因为对应的user对象的标签没有包含js；
	 * 所以根据这个标签找不到对应的用户
	 *
	 * @param tagNameList 用户传递过来的标签列表
	 * @return
	 */
	@Override
	public List<User> searchUsersByTags(List<String> tagNameList) {
		// 如果 传过来的集合为 空集合，那么记得返回自定义的异常错误
		if (CollectionUtils.isEmpty(tagNameList)) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR);
		}
		// 1. 先查询所有用户
		QueryWrapper<User> queryWrapper = new QueryWrapper<>();
		List<User> userList = userMapper.selectList(queryWrapper);
		Gson gson = new Gson();
		// 2. 在内存中判断是否包含要求的标签
		return userList.stream().filter(user -> {
			// 注意：我们在这个Tages字段中写的是 JSON类型的数据
			String tagsStr = user.getTags();
			// 下面这行代码很简单：使用了Gson库来将一个JSON字符串tagsStr解析为一个Set<String>对象。
			//具体来说，gson.fromJson()方法用于将JSON字符串转换为Java对象。
			// 在这里，它接受两个参数：要解析的JSON字符串和要转换成的Java对象的类型。
			Set<String> tempTagNameSet = gson.fromJson(tagsStr, new TypeToken<Set<String>>() {
			}.getType());
			tempTagNameSet = Optional.ofNullable(tempTagNameSet).orElse(new HashSet<>());
			for (String tagName : tagNameList) {
				if (!tempTagNameSet.contains(tagName)) {
					// 返回false，当前流元素不符合 filter的条件
					return false;
				}
			}
			// 该标签 符合要求单独过滤出来
			return true;
		}).map(this::getSafetyUser).collect(Collectors.toList());
	}

	/**
	 * 更新用户信息
	 *
	 * @param user      前端传过来要修改的用户
	 * @param loginUser 当前登录的用户
	 * @return
	 */
	@Override
	public int updateUser(User user, User loginUser) {
		// 拿到要修改的   用户的id
		long userId = user.getId();
		// 不用包装类就可以不进行非空判断了，但是还是要进行 id是否 >0的判断
		if (userId <= 0) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR);
		}
		// 如果是管理员，允许更新任意用户
		// 如果不是管理员，只允许更新当前自己的信息
		if (!isAdmin(loginUser) && userId != loginUser.getId()) {
			// 如果它不是管理员  且 当前 传过来要修改的用户id和 登录的用户id不一样的话，抛未登录异常
			throw new BusinessException(ErrorCode.NO_AUTH);
		}
		// 根据传过来的用户id，查到数据库对应的实体user
		User oldUser = userMapper.selectById(userId);
		// 非空判断
		if (oldUser == null) {
			throw new BusinessException(ErrorCode.NULL_ERROR);
		}
		// 根据这个实体，调用mapper，可以修改自己的用户数据了
		return userMapper.updateById(user);
	}


	/**
	 * 根据传递过来的登录态中的用户  判断 是否为管理员
	 *
	 * @param request
	 * @return
	 */
	@Override
	public boolean isAdmin(HttpServletRequest request) {
		// 仅管理员可查询
		Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
		User user = (User) userObj;
		return user != null && user.getUserRole() == ADMIN_ROLE;
	}

	/**
	 * 判断登录对象是否为管理员
	 *
	 * @param loginUser
	 * @return
	 */
	@Override
	public boolean isAdmin(User loginUser) {
		return loginUser != null && loginUser.getUserRole() == ADMIN_ROLE;
	}

	/**
	 * 在index页面查看 推荐用户（分页d）  -- 获取分页对象
	 * @param pageNum
	 * @param pageSize
	 * @return
	 */
	@Override
	public Page<User> getUserPage(long pageNum, long pageSize, HttpServletRequest request) {
		User loginUser = this.getLoginUser(request);
		// String.format() 方法用于将字符串模板中的 %s 占位符替换为实际的用户ID，从而得到最终的Redis键。
		String redisKey = String.format("partnerMatch:userService:recommend:%s", loginUser.getId());
		// 如果有缓存，直接读缓存
		// 将ValueOperations理解为redis操作value的一个对象，可以指定泛型）
		ValueOperations<String, Object> valueOperations = redisTemplate.opsForValue();
		Page<User> userPage =(Page<User>) valueOperations.get(redisKey);
		if (userPage != null) {
			// 说明有缓存，直接取userPage对象
			return userPage;
		}

		// 无缓存，查数据库

		// 先设置一个空条件，然后查询所有用户 ，把所有用户全部展示（记得用getsafe()方法脱敏）
		QueryWrapper<User> queryWrapper = new QueryWrapper<>();
		// Ipage是接口，page类是其Ipage接口的实现类
		// (pageNum - 1) * pageSize计算出了要查询的数据在数据库中的起始位置。然后，pageSize表示每页的数据量。
		/**
		 *  注意分页查询：
		 *  要查询的数据在数据库中的起始位置（例如表第一行对应的索引为0），即偏移量 （偏移量我可以理解为在数据表中该行对应着的索引位置）
		 *  (pageNum - 1) * pageSize 是为了计算偏移量，确保从正确的位置开始查询数据。
		 *  在MySQL中，这个偏移量将被用作LIMIT子句的第一个参数，指定从第几行开始返回数据。
		 *
		 *  pageNum  = 1, pageSize = 10 例如传递过来这个，表示查 第一页， 10条数据
		 *  对应映射的sql为：SELECT * FROM table_name LIMIT 0, 10;
		 *   (pageNum - 1) * pageSize这里 * pageSize是因为：确定每页要返回的数据数量。例如 pageNum  = 2, pageSize = 10
		 *   （2-1） * 10 = 10； 索引10表示从  表中第11行返回数据
		 */
		// limit在mysql中就是为了分页查询，所以 mybatisplus中的的page方法对应 limit的sql（有两个参数，分别是偏移量（当前行的索引）和行数
		userPage = userMapper.selectPage(new Page<>(pageNum, pageSize), queryWrapper);
		//
//		getRecords 方法的存在是为了方便获取当前页的数据列表
		List<User> records = userPage.getRecords();
		log.info("当前页的数据信息是：{}\n", records);
		// 一般我们每次查完mysql数据库后，我们都会存在redis缓存当中（下次就不用再查了）
		// 写缓存, 并设置redis的过期时间（因为redis的内存不能无限增加）
		try {
			// TimeUnit是Java中时间单位的一个枚举类
			valueOperations.set(redisKey, userPage, 30000, TimeUnit.MILLISECONDS);
		} catch (Exception e) {
			log.error("redis set key error", e);
		}
		return userPage;
	}


	/**
	 * 获取当前登录的用户信息
	 *
	 * @param request
	 * @return
	 */
	@Override
	public User getLoginUser(HttpServletRequest request) {
		if (request == null) {
			return null;
		}
		// 根据 request对象拿到用户的登录态
		Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
		// 2. 校验权限
		// 获取当前登录的用户信息

		if (userObj == null) {
			throw new BusinessException(ErrorCode.NO_AUTH);
		}
		return (User) userObj;

	}
}
