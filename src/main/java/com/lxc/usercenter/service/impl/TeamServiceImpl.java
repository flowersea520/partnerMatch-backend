package com.lxc.usercenter.service.impl;
import java.util.Date;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lxc.usercenter.common.ErrorCode;
import com.lxc.usercenter.exception.BusinessException;
import com.lxc.usercenter.mapper.TeamMapper;
import com.lxc.usercenter.model.domain.Team;
import com.lxc.usercenter.model.domain.User;
import com.lxc.usercenter.model.domain.UserTeam;
import com.lxc.usercenter.model.dto.TeamQueryDTO;
import com.lxc.usercenter.model.enums.TeamStatusEnum;
import com.lxc.usercenter.model.request.TeamJoinRequest;
import com.lxc.usercenter.model.request.TeamUpdateRequest;
import com.lxc.usercenter.model.vo.TeamUserVO;
import com.lxc.usercenter.model.vo.UserVO;
import com.lxc.usercenter.service.TeamService;
import com.lxc.usercenter.service.UserService;
import com.lxc.usercenter.service.UserTeamService;
import jodd.util.StringUtil;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

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
		team.setUserId(userId);
		boolean result = this.save(team);
		Long teamId = team.getId();
		if (!result || teamId == null) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR, "创建队伍失败");
		}

		//  e.  插入用户 => 队伍关系到关系表（所以调用对应的service/mapper，）
		UserTeam userTeam = new UserTeam();
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
				statusEnum = TeamStatusEnum.PUBLIC;
			}
			// 如果不是管理员，且队伍 不是公开的状态（那就是非公开的队伍），你们抛异常，因为没有权限
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
	 *  判断传入的 要修改的请求实体队伍，是否和之前的 队伍实体是否一致，一致就不修改
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
		if (teamId == null || teamId <= 0) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR);
		}
		// 通过id去查数据库，查到指定队伍（肯定team表，所以调用对应的service）
		Team team = this.getById(teamId);
		// 队伍必须存在
		if (team == null) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍必须存在");
		}
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
		// 所以只能用到第三张关联表，user_team表，
		userTeamQueryWrapper = new QueryWrapper<>();
		// 查询的where条件就是 teamId字段 = teamId，然后记录数量
		// 限定了这个条件，就能看到这个队伍有多少不同的用户的
		userTeamQueryWrapper.eq("teamId", teamId);
		long teamJoinNum = userTeamService.count(userTeamQueryWrapper);
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




