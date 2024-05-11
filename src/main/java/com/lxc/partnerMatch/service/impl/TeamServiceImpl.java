package com.lxc.partnerMatch.service.impl;

import java.util.Date;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lxc.partnerMatch.common.ErrorCode;
import com.lxc.partnerMatch.contant.DistributedLockKeys;
import com.lxc.partnerMatch.exception.BusinessException;
import com.lxc.partnerMatch.mapper.TeamMapper;
import com.lxc.partnerMatch.model.domain.Team;
import com.lxc.partnerMatch.model.domain.User;
import com.lxc.partnerMatch.model.domain.UserTeam;
import com.lxc.partnerMatch.model.dto.TeamQueryDTO;
import com.lxc.partnerMatch.model.enums.TeamStatusEnum;
import com.lxc.partnerMatch.model.request.TeamJoinRequest;
import com.lxc.partnerMatch.model.request.TeamQuitRequest;
import com.lxc.partnerMatch.model.request.TeamUpdateRequest;
import com.lxc.partnerMatch.model.vo.TeamUserVO;
import com.lxc.partnerMatch.model.vo.UserVO;
import com.lxc.partnerMatch.service.TeamService;
import com.lxc.partnerMatch.service.UserService;
import com.lxc.partnerMatch.service.UserTeamService;
import jodd.util.StringUtil;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.collections.CollectionUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @author lxc
 * @description 针对表【team(队伍表)】的数据库操作Service实现
 * @createDate 2024-04-26 01:15:35
 */
@Service
public class TeamServiceImpl extends ServiceImpl<TeamMapper, Team> implements TeamService {
	// 这个操作 用户队伍关系表的（第三张表）
	// 注意：在service中调用service是很常见的，可以这样做1
	@Resource
	private UserTeamService userTeamService;
	@Resource
	private UserService userService;

	@Resource
	private RedissonClient redissonClient;

	/**
	 * 创建队伍
	 *
	 * @param team
	 * @param loginUser
	 * @return
	 */
	@Override
	// 在调用该方法时应该启动一个事务，并且在方法执行过程中，如果抛出Exception异常，事务应该回滚。
	@Transactional(rollbackFor = Exception.class)
	public long addTeam(Team team, User loginUser) {
		//   a.  请求参数	是否为空
		if (team == null) {
			throw new BusinessException(ErrorCode.NULL_ERROR);
		}
		//  b.  是否登录，未登录不允许创建
		if (loginUser == null) {
			throw new BusinessException(ErrorCode.NOT_LOGIN);
		}
		// 这里用个常量final修饰，表示这个值我们不会变
		// 由于是方法内部的常量，没必要大写，如果是方法外部的，大写
		final long userId = loginUser.getId();
		//  c.  校验信息
		//    ⅰ. 队伍人数 > 1，且 <= 20
		// 这段代码就是让其如果不为空就取team.getMaxNum()，为空就为0
		int maxNum = Optional.ofNullable(team.getMaxNum()).orElse(0);
		if (maxNum < 1 || maxNum > 20) {
			// 队伍超过最大人数20个人，就抛异常
			throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍人数不满足要求");
		}
		//    ⅱ. 队伍名字（标题） 的长度 <= 20
		String name = team.getName();
		if (StringUtil.isBlank(name) || name.length() > 20) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍名字的长度不满足要求");
		}

		//    ⅲ. 描述 <= 512
		String description = team.getDescription();
		if (StringUtil.isNotBlank(description) && description.length() > 512) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR);
		}
		//    ⅳ. status是否公开（int）不传默认为0（公开）
		Integer status = Optional.ofNullable(team.getStatus()).orElse(0);
		// 从枚举类中获取 对应的枚举对象
		TeamStatusEnum statusEnum = TeamStatusEnum.getEnumByValue(status);
		if (statusEnum == null) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR);
		}
		//    ⅴ. 如果status是加密状态，一定要有密码，且密码 <= 32
		String password = team.getPassword();
		// equals() 方法是 Object 类中的方法，在所有类中都可以使用
		// 即比较两个对象的引用是否相同。（这里比较两个枚举对象是否相同）
		if (TeamStatusEnum.SECRET.equals(statusEnum)) {
			if (StringUtil.isBlank(password) || password.length() > 32) {
				throw new BusinessException(ErrorCode.PARAMS_ERROR, "");
			}
		}
		//    ⅵ. 超时时间 > 当前时间
		Date expireTime = team.getExpireTime();
		if (new Date().after(expireTime)) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR, "当前时间 > 超时时间");
		}
		//    ⅶ. 校验用户只能创建5个队伍
		// todo 有bug， 可能同时创建100个队伍
		QueryWrapper<Team> queryWrapper = new QueryWrapper<>();
		queryWrapper.eq("userId", userId);
		// 根据这个用户userId 在team队伍表中，看看有几个记录（相对应的几个队伍）
		long hasTeamNum = this.count(queryWrapper);
		if (hasTeamNum >= 5) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR, "最多创建5个队伍");
		}

		/**
		 * 这里涉及多次插入，记得添加事务，保证同时成功或者失败
		 */
		//  d.  插入队伍信息到队伍表
		// 通过将 id 属性设置为 null，你可以确保在保存操作时，
		// 不会使用对象中已有的 id 值，而是让数据库自动生成一个新的主键值。
		team.setId(null);
		// 所以team队伍表中的userId就是创建这个队伍的用户Id
		team.setUserId(userId);
		boolean result = this.save(team);
		Long teamId = team.getId();
		if (!result || teamId == null) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR, "创建队伍失败");
		}

		//  e.  插入用户 => 队伍关系到关系表（所以调用对应的service/mapper，）
		UserTeam userTeam = new UserTeam();
		// 所以team队伍表中的userId就是创建这个队伍的用户Id，我们也插入到第三张表中去
		userTeam.setUserId(userId);
		userTeam.setTeamId(teamId);
		userTeam.setJoinTime(new Date());
		result = userTeamService.save(userTeam);
		if (!result) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR, "创建队伍失败");
		}

		return teamId;
	}

	/**
	 * 查询队伍列表
	 *
	 * @param teamQueryDTO
	 * @return
	 */
	@Override
	public List<TeamUserVO> listTeams(TeamQueryDTO teamQueryDTO, boolean isAdmin) {
		// 因为我们要查询数据库，所以泛型最好写完整的 实体类
		QueryWrapper<Team> queryWrapper = new QueryWrapper<>();
		// 这里可以完全在mapper映射文件中写，但是我们这里用代码程序的形式去写
		// 1. 组合查询条件
		if (teamQueryDTO != null) {
			// 根据队伍的id描述来查询
			Long id = teamQueryDTO.getId();
			if (id != null) {
				// 如果对应属性不为null，我们拼sql
				queryWrapper.eq("id", id);
			}
			// IN 运算符在 MySQL 中的作用是用来判断某个值是否在一个给定的值列表中，并返回匹配的记录。
			List teamIdList = teamQueryDTO.getTeamIdList();
			if (CollectionUtils.isNotEmpty(teamIdList)) {
				// 队伍team表中 id 列与 teamIdList 中的值进行匹配，匹配成功返回对应的 记录
				// 构建sql：SELECT * FROM team WHERE id IN (value1, value2, ...);
				queryWrapper.in("id", teamIdList);
			}
			// 根据队伍的搜索关键字来查询
			String searchText = teamQueryDTO.getSearchText();
			if (StringUtil.isNotBlank(searchText)) {
				// and() 方法表示在当前查询条件的基础上添加一个 AND 连接的子条件。
				// 后面还调用了queryWrapper.and方法，都会进行and的sql拼接的
				// 我们这里根据 队伍名称 或者描述 进行 搜索关键字的模糊查询（用or，有一个满足就被查出来）
				queryWrapper.and(qw -> qw.like("name", searchText).or().like("description", searchText));
			}
			// 根据队伍名称来查询
			String name = teamQueryDTO.getName();
			if (StringUtil.isNotBlank(name)) {
				// 字符串就 like模糊查询
				queryWrapper.like("name", name);
			}

			// 根据队伍描述来查询
			String description = teamQueryDTO.getDescription();
			if (StringUtil.isNotBlank(description)) {
				queryWrapper.like("description", description);
			}
			// 根据 最大人数 来查询
			Integer maxNum = teamQueryDTO.getMaxNum();
			if (maxNum != null && maxNum <= 20 && maxNum > 0) {
				queryWrapper.eq("maxNum", maxNum);
			}
			// 根据 队伍的  用户id 来查询
			Long userId = teamQueryDTO.getUserId();
			if (userId != null && userId > 0) {
				queryWrapper.eq("userId", userId);
			}
			/**
			 * 根据状态 来查询
			 *   只有管理员才能查看加密还有非公开的 队伍
			 *   状态   0 - 公开， 1 - 私有，2 - 加密
			 */
			Integer status = teamQueryDTO.getStatus();
			// 获取状态数字对象 对应的枚举对象
			TeamStatusEnum statusEnum = TeamStatusEnum.getEnumByValue(status);
			// 如果获取的枚举对象是空的，那么手动给默认值（默认是public，公开的）
			if (statusEnum == null) {
				// 例如前端不传status状态属性，我们默认给这个放假设置为 公开的
				statusEnum = TeamStatusEnum.PUBLIC;
			}
			// 如果不是管理员，且队伍 不是公开的状态（那就是非公开的队伍），你们抛异常，因为没有权限
			// 这里不用 ||的原因：只要是管理员我们 就 可以访问 私有和 加密的队伍
			// 不是管理员只能访问公开和私有的队伍
			if (!isAdmin && !statusEnum.equals(TeamStatusEnum.PUBLIC)) {
				throw new BusinessException(ErrorCode.NO_AUTH, "不是管理员，没有权限访问");
			}
			// MyBatis-Plus 中的 eq 方法默认使用 AND 连接条件。
			// 当你调用 eq 方法时，它会将条件添加到查询中，并与其他条件一起使用 AND 连接起来。
			queryWrapper.eq("status", statusEnum.getValue());

		}
		// 注意事项:
		//主动调用or表示紧接着下一个方法不是用and连接!(不调用or则默认为使用and连接)
		//例: eq("id",1).or().eq("name","老王")--->id = 1 or name = '老王'
		// 通过 Lambda 表达式，可以将一个行为传递给另一个方法，并在需要时执行该行为。
//		它会在执行 and() 方法时被自动赋值为一个 QueryWrapper 对象。也就是说，lambda表达是的形参会被默认赋值
		// qw -> ... 是一个 Lambda 表达式，它接受一个 QueryWrapper 参数，用于构建子条件。
		/**
		 *  不展示已经过期的队伍
		 *  expireTime is null or expireTem > now()
		 *  下面代码转化为sql：
		 *  SELECT * FROM table_name
		 * WHERE （and） expireTime < CURRENT_TIMESTAMP OR expireTime IS NULL;
		 *
		 * queryWrapper.and(qw -> qw.gt("expireTime", new Date()).or().isNull("expireTime"))
		 * 是要满足gt和isNull的时候，才会添加到queryWrapper条件对象上吗
		 */
		// and会在之前的查询条件中 追加 and ... qw条件
		// 这个条件表示如果 expireTime 字段的值大于当前时间（没过期），或者 expireTime 字段的值为 NULL，就满足查询条件。
		queryWrapper.and(qw -> qw.gt("expireTime", new Date()).or().isNull("expireTime"));

		// list 方法用于根据条件查询多个记录。
		List<Team> teamList = this.list(queryWrapper);

		if (CollectionUtils.isEmpty(teamList)) {
			return new ArrayList<>();
		}
		ArrayList<TeamUserVO> teamUserVOList = new ArrayList<>();
		// 关联查询创建人的用户信息
		for (Team team : teamList) {
			// 拿到队伍中的用户id
			Long userId = team.getUserId();
			if (userId == null) {
				// 结束当前元素的 循环，
				// 开始下一个元素（下一个队伍，在判断下一个队伍的userId是否为null）
				continue;
			}
			// 这里在TeamService中调用 UserService相当于 通过team表的 userId属性 查询 user表
			User user = userService.getById(userId);
			// 创建一个 队伍和用户的封装类对象（（就是 team实体类的 脱敏版）
			TeamUserVO teamUserVO = new TeamUserVO();
			try {
				// 如果 team 对象中的属性比 TeamUserVO 对象的属性多，会默认将多余的属性丢弃
				BeanUtils.copyProperties(teamUserVO, team);
			} catch (Exception e) {
				throw new BusinessException(ErrorCode.SYSTEM_ERROR, "copyProperties方法错误");
			}

			try {
				if (user != null) {
					// 脱敏用户信息
					UserVO userVO = new UserVO();
					// 如果 team 对象中的属性比 TeamUserVO 对象的属性多，会默认将多余的属性丢弃
					BeanUtils.copyProperties(userVO, user);
					// 在对象拷贝的时候，user实体中没有createUser属性，所以默认为null
					// 我们通过set方法单独给其设置
					teamUserVO.setCreateUser(userVO);
				}

			} catch (Exception e) {
				throw new BusinessException(ErrorCode.SYSTEM_ERROR, "copyProperties方法错误");
			}
			// 添加到 一个集合当中，专门存放teamUserVo对象
			teamUserVOList.add(teamUserVO);
		}
		return teamUserVOList;
	}

	/**
	 * 更新队伍 （顾名思义：操作team表）
	 *
	 * @param teamUpdateRequest
	 * @param loginUser
	 * @return
	 */
	@Override
	public boolean updateTeam(TeamUpdateRequest teamUpdateRequest, User loginUser) {
		//   a.  判断请求参数是否为空  -- 其实Controller层已经判断了，但是我们还是要判断一下，保险一些，不会耗费什么性能的
		if (teamUpdateRequest == null) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR);
		}
		// 接收传过来更新队伍的request实体（dto），判断其id是否存在
		Long id = teamUpdateRequest.getId();
		Team updateTeam = new Team();
		if (id == null || id <= 0) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR);
		}
		//  b.  从数据库中 查询队伍是否存在
		// oldTeam可以理解为：从数据库查到的是 队伍（未修改前）
		Team oldTeam = this.getById(id);
		if (oldTeam == null) {
			throw new BusinessException(ErrorCode.NULL_ERROR);
		}
		//  c.  只有管理员 或者 队伍的创建者可以修改
		//  （如果加个！的话，那么这个或变成且）
		// 判断是否为队伍的创建者方法：
		// 对应mysql中的一对一的关系：team队伍表中的userId对应着user表的id
		if (!userService.isAdmin(loginUser) && (oldTeam.getUserId() != loginUser.getId())) {
			throw new BusinessException(ErrorCode.NO_AUTH, "只有管理员或者队伍的创建者可以修改");
		}
		//  d.  如果队伍是公开的，则密码不可修改
		Integer status = teamUpdateRequest.getStatus();
		TeamStatusEnum statusEnum = TeamStatusEnum.getEnumByValue(status);
		if (statusEnum.equals(TeamStatusEnum.PUBLIC) && statusEnum != null) {
//			todo 密码不可修改
			// 如果队伍是公开状态，则将 updateTeam 对象的密码属性设置为原始值
			updateTeam.setPassword(oldTeam.getPassword());
//			throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍是公开的，密码不可修改");
		}

		// 如果是加密的队伍， 则对应的密码不能为空
		if (statusEnum.equals(TeamStatusEnum.SECRET)) {
			if (StringUtil.isBlank(teamUpdateRequest.getPassword())) {
				throw new BusinessException(ErrorCode.PARAMS_ERROR, "加密队伍，密码不能为空");
			}
		}

		//  e.  如果用户传入的新值和老值一致，就不用update了
		//(可自行实现，降低数据库使用次数)

		try {
			BeanUtils.copyProperties(updateTeam, teamUpdateRequest);
		} catch (Exception e) {
			throw new BusinessException(ErrorCode.SYSTEM_ERROR, "copyProperties方法错误");
		}
		// 记得  把 Team 类重写 equals 和 hashCode 方法以比较对象的属性值。
		// 要不然默认比较对象的地址（即使 属性值一样还是false）
//		if (updateTeam.equals(oldTeam)) {
		// 检查传入的请求是否包含任何需要更新的字段
		if (!isUpdateRequired(teamUpdateRequest, oldTeam)) {
			// 如果没有字段需要更新，则直接返回true
			return true;
		}
		// 新值和老值不一样，所以操作数据库
		return this.updateById(updateTeam);
	}

	/**
	 * 判断传入的 要修改的请求实体队伍，是否和之前的 队伍实体是否一致，一致就不修改
	 *
	 * @param teamUpdateRequest
	 * @param oldTeam
	 * @return
	 */
	// 检查是否有字段需要更新的辅助方法
	public boolean isUpdateRequired(TeamUpdateRequest teamUpdateRequest, Team oldTeam) {
		// 检查每个字段是否与老值不同
		if (!Objects.equals(teamUpdateRequest.getDescription(), oldTeam.getDescription())) {
			return true;
		}
		if (!Objects.equals(teamUpdateRequest.getName(), oldTeam.getName())) {
			return true;
		}
		if (!Objects.equals(teamUpdateRequest.getPassword(), oldTeam.getPassword())) {
			return true;
		}
		if (!Objects.equals(teamUpdateRequest.getExpireTime(), oldTeam.getExpireTime())) {
			return true;
		}
		if (!Objects.equals(teamUpdateRequest.getStatus(), oldTeam.getStatus())) {
			return true;
		}
		// 如果所有字段都与老值相同，则不需要更新
		return false;
	}


	/**
	 * 加入队伍
	 *
	 * @param teamJoinRequest
	 * @return
	 */
	@Override
	// 如果数据库中有多个增删改的操作，最好都加上这个注解，避免脏数据
	@Transactional(rollbackFor = Exception.class)
	public boolean joinTeam(TeamJoinRequest teamJoinRequest, HttpServletRequest request) {
		if (teamJoinRequest == null) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR);
		}
		/**
		 * 优化代码的时候，尽量把 要查询数据库的代码放到后面
		 */
		// 1. 用户最多加入5个队伍
		// 通过登录的用户id，然后查询用户队伍的关联表，（单独查队伍team表也可以，但是可以体现一下多表查询的概念）
		User loginUser = userService.getLoginUser(request);
		long userId = loginUser.getId();

		/**
		 *  这里弄一个分布式锁
		 */
		//  锁对象：通常并不直接存储对象数据，而是通过控制对某个资源或代码段的访问权限来保证并发安全性。
		RLock lock = redissonClient.getLock(DistributedLockKeys.PARTNER_MATCH_JOINTEAM_LOCK_KEY);
		try {
//使用了一个 while (true) 循环，主要作用是持续尝试获取锁并执行相关业务逻辑，直到成功获取到锁为止（没拿到锁一直循环）。
// 这种方式通常用于实现一个持续运行的后台任务或者保持某个操作在锁定状态下持续执行直到成功为止。
			while (true) {
				// 第一个参数 0 是等待获取锁的最长时间（单位为毫秒），
				// 如果传入 0，表示立即尝试获取锁而不等待。如果传入一个正数，则表示在指定的时间内等待获取锁。
				// 第二个参数：锁的最长持有时间，即获取锁后，允许的最长持有时间。
				// 在指定的时间内未手动释放锁，则系统将自动释放锁。
				if (lock.tryLock(0, 30000L, TimeUnit.MILLISECONDS)) {
					System.out.println("getLock: " + Thread.currentThread().getId());
					// 这里是抢到锁后的 相关业务逻辑
					// 定义查询条件 (
					//这里的泛型 UserTeam 是用来指定查询条件的实体类类型
					//  这个泛型：通常对应数据库中的一张表。
					// 在这个例子中，UserTeam 可以被理解为对应数据库中的 UserTeam 表，因此查询条件会针对该表的记录进行构建。
					QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>();
					userTeamQueryWrapper.eq("userId", userId);
					// 根据条件判断 user_team表中（调用对应的service或者mapper），
					// 有多少条记录，就有多少个队伍
					// 查到的是：该用户 已加入队伍的数量
					long hasJoinNum = userTeamService.count(userTeamQueryWrapper);
					if (hasJoinNum > 5) {
						throw new BusinessException(ErrorCode.PARAMS_ERROR, "最多加入5个队伍");
					}
					// 判断要加入的队伍是否存在，则要拿到要加入队伍的id（唯一标识），
					Long teamId = teamJoinRequest.getTeamId();
					// 调用自定义的封装方法，根据id获取队伍, 并且进行非空判断校验
					Team team = getTeamById(teamId);

					// 拿到查询到的队伍过期时间
					Date expireTime = team.getExpireTime();
					// 未过期的队伍
					if (expireTime == null || expireTime.before(new Date())) {
						// // 过期时间 早于 当前时间（那就是过期了）
						throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍已过期了");
					}
					// 禁止加入私有的队伍（就根据队伍的状态码，然后找到对应的枚举值，判断是否是加密或者私有）
					Integer status = team.getStatus();
					// 获取对应的枚举对象
					TeamStatusEnum statusEnum = TeamStatusEnum.getEnumByValue(status);
					// 注意：调用equals方法的时候，非空的对象放在equals方法的左边
					if (TeamStatusEnum.PRIVATE.equals(statusEnum)) {
						throw new BusinessException(ErrorCode.PARAMS_ERROR, "禁止加入私有的队伍");
					}
					// 如果队伍是私密的，必须密码匹配（requestDTO对象中会携带队伍id和密码的）
					String joinTeamPassword = teamJoinRequest.getPassword();
					String password = team.getPassword();
					if (TeamStatusEnum.SECRET.equals(statusEnum)) {
						// 判断加入的队伍密码是否为空，且 加入队伍的DTO对象 密码是否等于 数据库中查到的队伍密码
						if (StringUtil.isBlank(joinTeamPassword) || !joinTeamPassword.equals(password)) {
							throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码错误");
						}
					}
					// 只能加入未满的队伍（所以这里查询这个队伍加入了多少用户）
					// 调用自己封装的接口
					long teamJoinNum = countTeamUserByTeamId(teamId);
					if (teamJoinNum > team.getMaxNum()) {
						// 大于了 队伍允许的最大 人数， 则队伍已满
						throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍已经满了");
					}

					// 不能重复加入已经加入的队伍
					// 例如：在第三张关联表中：userId = 1，队伍id = 1，那么这两个数据不能出现重复
					// 也就是对应的count <= 1
					// （比1大就是有多条了，也就是重复加入了，等于1就是已经加入队伍了）
					userTeamQueryWrapper = new QueryWrapper<>();
					userTeamQueryWrapper.eq("teamId", teamId);
					userTeamQueryWrapper.eq("userId", userId);
					long teamHasJoinNum = userTeamService.count(userTeamQueryWrapper);
					if (teamHasJoinNum >= 1) {
						throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户已经加入了队伍");
					}

					// 修改队伍信息
					UserTeam userTeam = new UserTeam();
					userTeam.setUserId(userId);
					userTeam.setTeamId(teamId);
					userTeam.setJoinTime(new Date());
					// 调用service，保存到 user_team表
					return userTeamService.save(userTeam);

				}
			}

		} catch (InterruptedException e) {
			log.error("partnerMatch:join_team：error");
			return false;
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


	/**
	 * 退出队伍
	 *
	 * @param teamQuitRequest
	 * @param loginUser
	 * @return
	 */
	@Override
	public boolean quitTeam(TeamQuitRequest teamQuitRequest, User loginUser) {
		if (teamQuitRequest == null) {
			throw new BusinessException(ErrorCode.NULL_ERROR);
		}
		// 拿到退出队伍请求的dto中的 队伍id，
		Long teamId = teamQuitRequest.getTeamId();
		// 调用自定义的封装方法，根据id获取队伍
		Team team = getTeamById(teamId);
		// 拿到登录的用户id，然后我们查询第三张 用户队伍关联表
		long userId = loginUser.getId();
		// new 一个实体，给queryWrapper构建 where条件
		UserTeam userTeam = new UserTeam();
		userTeam.setUserId(userId);
		userTeam.setTeamId(teamId);
		// 给queryWrapper传个实体，会根据实体的属性构建where条件
		// 这个QueryWrapper<泛型> ：泛型查询的哪个表的实体数据
		QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>(userTeam);
		// 在默认情况下，使用 QueryWrapper 构建的条件是 AND 关系。
		// 也就是说，如果你向 QueryWrapper 中传递了多个属性，它们将会被视为 AND 连接。
		// 这里要使用到第三张表 user-team表
		long count = userTeamService.count(userTeamQueryWrapper);
		if (count == 0) {
			// userId和teamId两个条件同时满足的记录数为 0，所以 没有加入队伍
			throw new BusinessException(ErrorCode.PARAMS_ERROR, "没有加入队伍");
		}
		// 调用自定义接口，查询该 队伍有多少个用户
		long teamHasJoinNum = countTeamUserByTeamId(teamId);
		// 要退出的队伍最后一人，那么我们就删除这个队伍
		if (teamHasJoinNum == 1) {
			// 删除队伍，记得删除队伍表以及 队伍用户的关系表
			this.removeById(teamId);
			// 删除 队伍用户的关系表（就是把 对应teamId相关的记录条数都删了，因为队伍都不存在了）
			// 注意：这里不能使用 removeById，因为他是根据 队伍用户的关系表的id来删
			// 而不是根据 teamId来删，所以我们要指定queryWrapper，构建where条件
			userTeamQueryWrapper = new QueryWrapper<>();
			userTeamQueryWrapper.eq("teamId", teamId);
			return userTeamService.remove(userTeamQueryWrapper);
		} else {
			//     ⅱ. 还有其他人
			// 判断是否为队长， （数据库这里默认用户id，就是队伍的队长id）
			if (team.getUserId() == userId) {
				// 1.  如果是队长退出队伍，队长转移给第二早加入队伍的人——先来后到
				//  查询已加入队伍的所有用户和加入时间
				userTeamQueryWrapper = new QueryWrapper<>();
				userTeamQueryWrapper.eq("teamId", teamId);
				// last方法用于添加一个 SQL 片段到查询中，无视优化规则直接拼接到 sql 的最后
				// 这里指定了按照 id 升序排序并限制结果集为两条记录。
				userTeamQueryWrapper.last("order by id asc limit 2");
				// 将查询条件应用到数据库表中，并返回满足条件的记录列表。
				List<UserTeam> userTeamList = userTeamService.list(userTeamQueryWrapper);
				if (CollectionUtils.isEmpty(userTeamList) || userTeamList.size() <= 1) {
					// 查询到的记录空记录，或者 记录 <= 1
					throw new BusinessException(ErrorCode.SYSTEM_ERROR);
				}
				// 获取记录中的第二条数据（user-team表中，先插入的表id就小，后插入的表id就大）
				// 因为是自增的，获取第二条数据，就是取索引为 1
				// 下一个用户队伍的关系记录数
				UserTeam nextUserTeam = userTeamList.get(1);
				// 获取新队长的用户id（也就是队长id）
				Long nextTeamLeaderId = nextUserTeam.getUserId();
				// 更新当前的队伍队长（通过队长id取更新，操作team表）
				Team updateTeam = new Team();
				// 更新队伍表的id, 所对应的 行记录
				updateTeam.setId(teamId);
				// 更新队伍表的 userId，也就是队长id
				updateTeam.setUserId(nextTeamLeaderId);
				// updateById 将传入的实体对象中非空的属性字段值更新到数据库中相应的记录中。·
				// updateById 用于根据实体对象的 主键 ( 在这里是 teamId 来定位数据库中的相应记录。) 来更新数据库记录。
				boolean result = this.updateById(updateTeam);
				if (!result) {
					// 更新失败
					throw new BusinessException(ErrorCode.PARAMS_ERROR, "更新队伍失败");
				}
				// 删除了 team队伍表后，也要删除对应的 用户队伍表（第三张表）
				userTeamQueryWrapper = new QueryWrapper<>();
				userTeamQueryWrapper.eq("teamId", teamId);
				// 这个用户id的获取就是 从 登录的用户获取的（登录的用户来操作 退出队伍）
				userTeamQueryWrapper.eq("userId", userId);
				// 删除对应的 用户队伍表（第三张表）
				return userTeamService.remove(userTeamQueryWrapper);
			} else {
				// 队伍还有其他人，且退出队伍的人不是队长，自己退出队伍
				// todo 这里其实可以移到外面去，因为重复代码，但是我们这里为了逻辑清晰，所以就这样写
				return userTeamService.remove(userTeamQueryWrapper);
			}
		}
	}

	/**
	 * 通过id去 获取队伍（查数据库的team队伍表）
	 *
	 * @param teamId
	 * @return
	 */
	private Team getTeamById(Long teamId) {
		if (teamId == null || teamId <= 0) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR);
		}
		// 根据队伍id，查询队伍表
		Team team = this.getById(teamId);
		if (team == null) {
			throw new BusinessException(ErrorCode.NULL_ERROR, "队伍不存在");
		}
		return team;
	}

	/**
	 * 解散（删除）队伍
	 *
	 * @param id        要删除的队伍id
	 * @param loginUser
	 * @return
	 * @Transactional(rollbackFor = Exception.class)
	 * 在事务管理中，为了确保数据的一致性，通常会将一系列操作作为一个事务来执行，如果其中的任何一个操作失败，
	 * 整个事务会被回滚，从而保证了数据的完整性。因此，事务管理可以有效地避免脏数据的产生。
	 */
	@Override
	// 当方法执行过程中出现异常时，事务会被回滚，从而避免脏数据的产生。
	// 脏数据通常指的是数据不一致的情况。在数据库操作中，
	// 如果因为某种原因导致数据的一部分被删除而另一部分未被删除，或者数据的某些属性处于不一致的状态，都可以被称为脏数据。
	@Transactional(rollbackFor = Exception.class)
	public boolean deleteTeam(long id, User loginUser) {
		//  b. 校验队伍是否存在（ 调用自定义的封装方法，根据id获取队伍，内部进行非空校验）
		Team team = getTeamById(id);
		long teamId = team.getId();
		//  c. 校验你是不是队长（只要是对 数据库进行操作，都可以把用户传进来，因为要校验权限）
		if (team.getUserId() != loginUser.getId()) {
			throw new BusinessException(ErrorCode.NO_AUTH, "你不是队伍的队长，无法解散队伍");
		}
		//  d. 移除所有加入队伍的关联信息（把有关的表的信息都删除）
		QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>();

		userTeamQueryWrapper.eq("teamId", teamId);

		// 删除对应的 用户队伍表（第三张表）
		boolean result = userTeamService.remove(userTeamQueryWrapper);
		if (!result) {
			// 如果删除失败了，抛异常
			throw new BusinessException(ErrorCode.PARAMS_ERROR, "删除用户队伍关联信息失败");
		}
		//  e. 删除队伍   删除team表
		return this.removeById(teamId);

	}


	/**
	 * 查询user-team用户队伍关系表中：一个队伍有多少的用户
	 *
	 * @param teamId
	 * @return
	 */
	private long countTeamUserByTeamId(Long teamId) {
		QueryWrapper<UserTeam> userTeamQueryWrapper;
		// 只能加入未满的队伍（所以这里查询这个队伍加入了多少用户）
		// 所以只能用到第三张关联表，user_team表，
		userTeamQueryWrapper = new QueryWrapper<>();
		// 查询的where条件就是 teamId字段 = teamId，然后记录数量
		// 限定了这个条件，就能看到这个队伍有多少不同的用户的
		userTeamQueryWrapper.eq("teamId", teamId);
		return userTeamService.count(userTeamQueryWrapper);
	}
}




