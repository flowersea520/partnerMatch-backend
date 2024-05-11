package com.lxc.partnerMatch.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.lxc.partnerMatch.common.ErrorCode;
import com.lxc.partnerMatch.contant.RedisKeys;
import com.lxc.partnerMatch.exception.BusinessException;
import com.lxc.partnerMatch.model.domain.User;
import com.lxc.partnerMatch.service.UserService;
import com.lxc.partnerMatch.mapper.UserMapper;
import com.lxc.partnerMatch.utils.AlgorithmUtil;
import javafx.util.Pair;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.lxc.partnerMatch.contant.UserConstant.*;

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
	 * 将登录的用户id，存储到 缓存的key当中， 而value就是page对象
	 *
	 * @param pageNum
	 * @param pageSize
	 * @return
	 *  注意：注意这里没有用锁的概念，就是将 从数据库查到的对象，放到 redis 的key中，用作缓存，
	 *  这里只是单纯的普及锁的概念
	 */
	@Override
	public Page<User> getUserPage(long pageNum, long pageSize, HttpServletRequest request) {
		User loginUser = this.getLoginUser(request);
		// String.format() 方法用于将字符串模板中的 %s 占位符替换为实际的用户ID，从而得到最终的Redis键。
		// 这个也是lockKey，lockKey 作为一个唯一的标识符用于在Redis中创建一个具有特定名称的锁。
		// 当在Redis上创建锁时，常见的做法是将这个锁作为一个键（key），
		// 并赋予其某种特殊值，以表明该锁当前的状态（已锁定或未锁定）。
		String redisKey = String.format(RedisKeys.LOGIN_USERID_LOCK, loginUser.getId());
		// 如果有缓存，直接读缓存
		// 将ValueOperations理解为redis操作value的一个对象，可以指定泛型）
		ValueOperations<String, Object> valueOperations = redisTemplate.opsForValue();
		// 拿到了 锁
		Page<User> userPage = (Page<User>) valueOperations.get(redisKey);
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
//			// 将这个锁作为一个键（key），并赋予其某种特殊值（这里是UserPage对象），
//			以表明该锁当前的状态（已锁定或未锁定）
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
			throw new BusinessException(ErrorCode.NOT_LOGIN);
		}
		return (User) userObj;

	}


	/**
	 * 根据登录的 用户，去匹配和其相似的 其他用户
	 *
	 * @param num
	 * @param loginUser
	 * @return
	 */
	@Override
	public List<User> matchUsers(long num, User loginUser) {
		// 指定查询条件，查询tags的字段，必须不为空
		QueryWrapper<User> queryWrapper = new QueryWrapper<>();
		// 主要查询 id和 tags字段 （会加快查询速度）
		queryWrapper.select("id", "tags");
		queryWrapper.isNotNull("tags");
		// 先查询数据库中的user表的 所有的用户
		List<User> userList = this.list(queryWrapper);
		// 拿到登录用户的标签，（里面存放是json字符串，所以我们这里的实体类的属性是String）
		String tags = loginUser.getTags();

		// Gson 是 Google 提供的一个用于处理 JSON 数据的 Java 库。
		// 它可以帮助你将 JSON 字符串转换为 Java 对象，也可以将 Java 对象转换为 JSON 字符串。
		// 如果要将 JSON 字符串转换为列表，最好使用 TypeToken。
		// 这样可以确保 Gson 正确地解析 JSON 字符串并生成相应类型的对象。
		Gson gson = new Gson();
		// 使用TypeToken来指定要转换的目标类型，因为Java的泛型在运行时会被擦除
		// 创建 TypeToken 以指定列表的类型
		Type listType = new TypeToken<List<String>>() {
		}.getType();

		// 将 JSON 字符串转换为 List  （这个是登录用户的标签列表）
		List<String> tagList = gson.fromJson(tags, listType);

		// 用户列表的下标 => 相似度
		// 使用Pair类的一个常见场景是当你需要从一个方法返回两个值，但Java的方法只能返回一个值时。
		// 通过使用Pair，你可以将两个值组合成一个对象并返回它。
		// Apache Commons Lang中的Pair类我可以理解为：存储键值对的对象的容器
		// 这里相当于将：User和Long类型的对象，组合成一个ArrayList的元素，
		ArrayList<Pair<User, Long>> list = new ArrayList<>();

		/**
		 *  说白了就是：当前登录的用户标签， 和数据库中每个用户的标签，进行相似度比较，然后存入到TreeMap集合中（会排序的）
		 */
		for (int i = 0; i < userList.size(); i++) {
			// 遍历每个user，如何拿到其对应的用户标签
			User user = userList.get(i);
			// 这里的String类型就是json字符串，在Java中用string存储
			String userTags = user.getTags();
			// 无标签（该字符串是空串），那么遍历下一个元素
			// 无标签  或者 该遍历的用户id，是自己，那跳出本次循环（因为自己不需要匹配自己）
			if (StringUtils.isBlank(userTags) || user.getId() == loginUser.getId()) {
				continue;
			}
			// 将 JSON字符串（这里用String接收），转为 List对象（就是标签变成集合的方式存储）
			// 这个userTagList是数据库中每个用户对象 的标签列表（每个用户都会遍历）
			List<String> userTagList = gson.fromJson(userTags, listType);
			// 计算 距离 分数（数值越小，越相似）
			// 计算出 当前登录用户的标签列表tagList  和 数据库中每个用户的标签列表userTagList 的相似度
			long distance = AlgorithmUtil.minDistance(tagList, userTagList);
			// 将对应的 key（是用户索引）得到的user对象，value（距离大小），存入到list集合中去
			// 	Apache Commons Lang中的Pair类我可以理解为：存储键值对的对象的容器
			list.add(new Pair<>(user, distance));

		}
		// 按照 编辑距离从小到大 升序排序
		List<Pair<User, Long>> topUserPairList = list.stream()
//				直接使用(a.getValue() - b.getValue())可能会导致精度问题或整数溢出（如果Long值很大），
				// 所以将其转换为 int
				// a和b是Comparator接口的compare方法的两个参数，代表了你想要比较的两个元素。（默认升序，要降序换ab位置）
				// 在这个特定的例子中，a和b都是Pair<User, Long>类型的对象，因为list是一个List<Pair<User, Long>>。
				.sorted((a, b) -> (int) (a.getValue() - b.getValue()))
				.limit(num)
				.collect(Collectors.toList());
		// lambda表达式，里面都是都是集合的元素（这里将每个pair元素的key取出来，里面都是user对象，取出id，然后构建出新的集合
		// 注意：这里的userId列表都是 匹配度相似的用户id
		List<Long> userIdList = topUserPairList.stream().map(pair -> {
			return pair.getKey().getId();
		}).collect(Collectors.toList());
		// 构建查询条件，根据id去查，看看id是否在其 userId列表范围内
		QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
		userQueryWrapper.in("id", userIdList);
		// Pair::getKey是一个方法引用，它表示从每一个Pair对象中提取其key
		// （即Pair中的第一个元素，在这里 Pair的getKey()方法返回的是User类型的对象）
		// 因此，这个操作会将原始的Stream<Pair<User, ...>>转换成一个Stream<User>。
		// 根据id列表，查询数据库，查出对应的user列表，然后都调用getSafetyUser脱敏
		/**
		 * 数据库引擎会根据自身的优化和处理方式返回结果集，可能会根据索引、数据分布等因素进行调整，
		 * 最终返回的结果可能与IN子句中指定值的顺序不同。因此，无法保证结果的顺序与IN子句中指定的值的顺序相同。
		 *  注意：由于上面那个 条件是in，如果是 in (1,3,2) ,
		 *  				那顺序可能还是 1，2，3
		 *  			所以我们进行对每个用户id分组，key就是用户id，value就是 用户列表（其实就一个用户元素）
		 *  			，因为用户id唯一
		 */
		Map<Long, List<User>> userIdUserListMap = this.list(userQueryWrapper).stream()
				// map方法根据 映射成 指定方法的 流对象
				// 调用 getSafetyUser 方法来处理这个 User 对象。
				.map(user -> getSafetyUser(user)).collect(Collectors.groupingBy(User::getId));
		// 用于存放最终要返回的 用户列表（匹配的num个用户对象列表）
		ArrayList<User> finalUserList = new ArrayList<>();
		for (Long userId : userIdList) {
			// 根据 匹配的用户id，从map集合中，获取对应的value，也就是用户列表（就一个元素，因为id唯一）
			finalUserList.add(userIdUserListMap.get(userId).get(0));
		}

		return finalUserList;


	}
}
