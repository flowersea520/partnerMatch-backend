package com.lxc.partnerMatch.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lxc.partnerMatch.common.BaseResponse;
import com.lxc.partnerMatch.common.DeleteRequest;
import com.lxc.partnerMatch.common.ErrorCode;
import com.lxc.partnerMatch.common.ResultUtils;
import com.lxc.partnerMatch.exception.BusinessException;
import com.lxc.partnerMatch.model.domain.Team;
import com.lxc.partnerMatch.model.domain.User;
import com.lxc.partnerMatch.model.domain.UserTeam;
import com.lxc.partnerMatch.model.dto.TeamQueryDTO;
import com.lxc.partnerMatch.model.request.TeamAddRequest;
import com.lxc.partnerMatch.model.request.TeamJoinRequest;
import com.lxc.partnerMatch.model.request.TeamQuitRequest;
import com.lxc.partnerMatch.model.request.TeamUpdateRequest;
import com.lxc.partnerMatch.model.vo.TeamUserVO;
import com.lxc.partnerMatch.service.TeamService;
import com.lxc.partnerMatch.service.UserService;
import com.lxc.partnerMatch.service.UserTeamService;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.beanutils.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author mortal
 * @date 2024/4/26 12:05
 */
@RestController

@RequestMapping("/team")

public class TeamController {
	@Resource
	private TeamService teamService;

	@Resource
	private UserService userService;

	@Resource
	private UserTeamService userTeamService;

	/**
	 * 创建 队伍操作，
	 * 一般是 传过来 实体类，然后返回 添加成功后的记录id作为data传给前端就可以了
	 *
	 * @param teamAddRequest
	 * @return
	 */
	@PostMapping("/add")
	@ApiOperation("用户创建队伍")
	public BaseResponse<Long> addTeam(@RequestBody TeamAddRequest teamAddRequest, HttpServletRequest request) {
		// 第一件事情就是非空判断，判断其传过来的数据 是否为空
		if (teamAddRequest == null) {
			throw new BusinessException(ErrorCode.NULL_ERROR);
		}
		User loginUser = userService.getLoginUser(request);
		Team team = new Team();
		try {
			BeanUtils.copyProperties(team, teamAddRequest);
		} catch (Exception e) {
			throw new BusinessException(ErrorCode.SYSTEM_ERROR, "copyProperties方法异常");
		}
		// 调用写好的service层的addTeam()方法
		long teamId = teamService.addTeam(team, loginUser);
		return ResultUtils.success(teamId);

	}


	/**
	 * 更新队伍：根据前端传过来的实体类（修改后的实体类，让我们更新数据库的），
	 * 修改这个数据库后返回给前端一个Boolean值就可以了，表示我们修改成功了
	 *
	 * @param teamUpdateRequest
	 * @return
	 */
	@PutMapping("/update")
	@ApiOperation("用户更新队伍")
	public BaseResponse<Boolean> updateTeam(@RequestBody TeamUpdateRequest teamUpdateRequest, HttpServletRequest request) {
		// 第一件事情就是非空判断，判断其传过来的数据 是否为空
		if (teamUpdateRequest == null) {
			throw new BusinessException(ErrorCode.NULL_ERROR);
		}
		User loginUser = userService.getLoginUser(request);
		// 根据传过来修改后的实体类，然后我们根据这个id取修改
		boolean result = teamService.updateTeam(teamUpdateRequest, loginUser);
		if (!result) {
			// 如果修改失败
			throw new BusinessException(ErrorCode.SYSTEM_ERROR, "更新失败");
		}
		return ResultUtils.success(true);

	}

	/**
	 * 查找单个队伍 根据 传过来的 id 进行查找，返回一个实体对象
	 *
	 * @param id
	 * @return
	 */
	@GetMapping("/get")
	@ApiOperation("查找单个队伍")
	// 基本类型（如 int、String 等）作为方法参数时，最好使用 @RequestParam 注解，以明确表明这是一个请求参数。
	// 而对于复杂的参数结构，可以直接使用对象类型作为方法参数，
	// Spring MVC 会尝试从请求中URL 的 查询参数 或 请求体中 解析出对象的属性值，从而进行绑定。
	public BaseResponse<Team> getTeam(@RequestParam long id) {
		// 第一件事情就是非空判断，判断其传过来的数据 是否为空
		if (id < 0) {
			throw new BusinessException(ErrorCode.NULL_ERROR);
		}
		Team team = teamService.getById(id);
		if (team == null) {
			// 如果查找的数据为null
			throw new BusinessException(ErrorCode.SYSTEM_ERROR, "查找失败");
		}
		return ResultUtils.success(team);

	}

	/**
	 * 查找多个队伍，传过来一个封装好的DTO（这个DTO专门用来查询队伍的一个实体），返回给前端一个 队伍列表
	 *
	 * @param teamQueryDTO
	 * @return
	 */
//	@GetMapping("/list")
//	// 如果方法参数类型是一个对象（比如 TeamQuery），
//	// 而没有使用 @RequestParam 注解，Spring MVC 会尝试将请求中URL的 查询参数 映射到方法参数对象的属性上。
//	// 这样做 更适用于 复杂的 参数结构，比如包含多个字段的对象。
//	// 如果是普通的基本类型，最好用一下@RequestParams注解，引用类型可以不用，效果都一样
//	public BaseResponse<List<Team>> listTeams(TeamQueryDTO teamQueryDTO) {
//		if (teamQueryDTO == null) {
//			throw new BusinessException(ErrorCode.NULL_ERROR);
//		}
//		Team team = new Team();
//		try {
//			BeanUtils.copyProperties(team, teamQueryDTO);
//		} catch (Exception e) {
//			throw new BusinessException(ErrorCode.SYSTEM_ERROR);
//		}
//
//		// 当你创建了一个 QueryWrapper<Team> 对象，并将 team 对象传递给它时，
//		// QueryWrapper 会根据 team 对象中的 属性值 来构建 查询 条件。
//		// 例如，如果 team 对象的某个属性值不为空，那么查询条件就会包含对应的条件。
//		// team 对象通常用作查询条件的封装。
//		QueryWrapper<Team> queryWrapper = new QueryWrapper<>(team);
//		// 注意：如果 Team 类中的 id 属性是 1 "，那么 QueryWrapper 将会根据这个属性值构建相应的查询条件。
////		SELECT * FROM Team WHERE id = 1;
//		List<Team> teamList = teamService.list(queryWrapper);
//		return ResultUtils.success(teamList);
//	}
	@GetMapping("/list")
	@ApiOperation("查询队伍列表")
	// VO 是用于呈现视图（View）的对象。它通常用于控制器层（Controller）向 前端 页面传递数据。
	// 这里的TeamUserVO有点类似于 脱敏之后的 Team实体类 ，所以专门用于返回给前端
	// 这里不用@Params注解，默认就是这个，如果传过来参数，那么会将 参数对应的映射的实体属性上
	public BaseResponse<List<TeamUserVO>> listTeams(TeamQueryDTO teamQueryDTO, HttpServletRequest request) {
		if (teamQueryDTO == null) {
			throw new BusinessException(ErrorCode.NULL_ERROR);
		}
		// 根据传过来的request对象，判断是否是管理员
		boolean isAdmin = userService.isAdmin(request);
		// （就是 team实体类的 脱敏版）
		// 返回后端查询 符合 要求条件（这里说的条件是传递的参数然后映射导DTO上，作为sql的条件）的队伍列表
		// 加入队伍的队伍列表
		List<TeamUserVO> teamList = teamService.listTeams(teamQueryDTO, isAdmin);
		/**
		 * 判断当前用户是否 已 加入队伍（将所有符合要求的 队伍id，单独存放在一个列表当中）
		 */
		List<Long> teamIdList;
		try {
			// （目的：就是为了方便前端： 不是队伍的创建者，也就不是队长，那就会出现退出按钮）
			// 将所有的 符合条件的 队伍列表 的id，全部放到 单独映射出来放到一个新的集合里面
			// 这里不用set的原因是：我们能够确定 的id就是唯一的，不需要set集合去重
			// 这个teamIdList就是：加入队伍的队伍id列表
			teamIdList = teamList.stream().map(TeamUserVO::getId).collect(Collectors.toList());
			// 这里 的泛型用 UserTeam的目的就是为了查 userteam表：
			QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>();
			// 更具request对象，拿到登录的用户
			User loginUser = userService.getLoginUser(request);
			// 构建两个条件，1. 当前 UserTeam的userid = 登录的用户id
//						2. 当前的  teamIdList 列表内 是否存在于 UserTeam表的 teamId字段中，如果存在返回 所有的对象列表
			userTeamQueryWrapper.eq("userId", loginUser.getId());
			userTeamQueryWrapper.in("teamId", teamIdList);
			// 返回 所有符合这两个条件的 对象列表
			List<UserTeam> userTeamList = userTeamService.list(userTeamQueryWrapper);
			// 已 加入队伍的 id集合（这里用set收集，因为要去重，我们不用看重复的队伍id）
			Set<Long> hasJoinTeamIdSet = userTeamList.stream().map(UserTeam::getTeamId).collect(Collectors.toSet());
			// 遍历 符合 要求条件（这里说的条件是传递的参数然后映射导DTO上，作为sql的条件）的队伍列表
			teamList.forEach(team -> {
				// contains方法 它用于检查指定的元素是否存在于 set集合中
				// 所以用来查看 队伍列表的id，是否在 已加入的set集合 队伍id列表中
				boolean hasJoin = hasJoinTeamIdSet.contains(team.getId());
				// 将这个 boolean对象，赋值给 UserTeamVo实体类，用来返回给前端看看，是否加入了
				team.setHasJoin(hasJoin);

			});

		} catch (Exception e) {
			// 我们就是为了防止他sql异常，所以我们加个try Catch
			throw new RuntimeException(e);
		}
		// 查询加入队伍的用户信息（人数）， 这个加入队伍的队伍id列表：teamIdList（自己在控制台写一下sql）
		QueryWrapper<UserTeam> userTeamJoinQueryWrapper = new QueryWrapper<>();
		// teamIdList就是 TeamUserVO就是team表的 实体（脱敏版）的id列表
		// 定义 通过加入队伍的队伍id，获取 加入队伍的 用户, 多个用户对应着多个队伍（操作第三张表--user_team）
		userTeamJoinQueryWrapper.in("teamId", teamIdList);
		List<UserTeam> userTeamList = userTeamService.list(userTeamJoinQueryWrapper);
		// 脑子里面想像：第三张表的多表记录：userTeamList;
		// 用 其表的teamId分组，然后形成map集合键值对，这样就可以一个表队伍id，对应着一个表记录列表（多条记录）（userTeam表实体）
		Map<Long, List<UserTeam>> teamIdUserTeamList
				= userTeamList.stream().collect(Collectors.groupingBy(UserTeam::getTeamId));
		// teamList就是 team表的多条记录数
		teamList.forEach(team -> {
			// getOrDefault 方法是 Java 中 Map 接口的一个方法，用于从映射中获取指定键对应的值；
			// 如果映射中不存在这个键，则返回一个默认值。这里要是id不存在，则返回 空集合
			// 取出对应的 teamId键对应的  value多条记录数，就是加入队伍的用户个数
			team.setHasJoinNum(teamIdUserTeamList.getOrDefault(team.getId(), new ArrayList<>()).size());
		});

		return ResultUtils.success(teamList);
	}


	/**
	 * 分页查询 队伍，还是传过来一个封装好的DTO（这个DTO专门用来查询队伍的一个实体），返回给前端一个 page对象
	 *
	 * @param teamQueryDTO
	 * @return
	 */
	@GetMapping("/list/page")
	@ApiOperation("分页查询队伍")
	// 默认情况下相当于使用了 @RequestParam 注解。@RequestParam 注解用于从请求的查询字符串中获取参数值
	// 会将请求 URL 中的参数绑定到方法的参数上。（不加@RequestBody相当于还是以 参数的请求方式，然后绑定到实体上）
	public BaseResponse<Page<Team>> listTeaamsByPage(TeamQueryDTO teamQueryDTO) {
		if (teamQueryDTO == null) {
			throw new BusinessException(ErrorCode.NULL_ERROR);
		}
		Team team = new Team();
		try {
			BeanUtils.copyProperties(team, teamQueryDTO);
		} catch (Exception e) {
			throw new BusinessException(ErrorCode.SYSTEM_ERROR);
		}

		Page<Team> page = new Page<>(teamQueryDTO.getPageNum(), teamQueryDTO.getPageSize());

		// 当你创建了一个 QueryWrapper<Team> 对象，并将 team 对象传递给它时，
		// QueryWrapper 会根据 team 对象中的 属性值 来构建 查询 条件。
		// 例如，如果 team 对象的某个属性值不为空，那么查询条件就会包含对应的条件。
		// team 对象通常用作查询条件的封装。
		QueryWrapper<Team> queryWrapper = new QueryWrapper<>(team);
		// 注意：如果 Team 类中的 name 属性被设置为 "梦之队"，那么 QueryWrapper 将会根据这个属性值构建相应的查询条件。
//		SELECT * FROM Team WHERE teamName = '梦之队';
		Page<Team> pageResult = teamService.page(page, queryWrapper);
		return ResultUtils.success(pageResult);
	}

	/**
	 * 加入队伍
	 *
	 * @return
	 */
	@PostMapping("/join")
	@ApiOperation("用户加入队伍")
	public BaseResponse<Boolean> joinTeam(@RequestBody TeamJoinRequest teamJoinRequest, HttpServletRequest request) {
		if (teamJoinRequest == null) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR);
		}
		// 调用业务层
		boolean result = teamService.joinTeam(teamJoinRequest, request);
		return ResultUtils.success(result);

	}


	/**
	 * 退出队伍
	 *
	 * @return
	 */
	@PostMapping("/quit")
	@ApiOperation("用户退出队伍")
	public BaseResponse<Boolean> quitTeam(@RequestBody TeamQuitRequest teamQuitRequest, HttpServletRequest request) {
		if (teamQuitRequest == null) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR);
		}
		// 拿到登录的用户，我们才知道谁要退出队伍
		User loginUser = userService.getLoginUser(request);
		// 调用业务层
		boolean result = teamService.quitTeam(teamQuitRequest, loginUser);
		return ResultUtils.success(result);

	}


	/**
	 * 解散队伍（从原来的删除队伍改造了一下），
	 * 这个一般是根据传过来的  id去 解散队伍（和删除一样）， 删除成功后返回一个Boolean类型的值就可以了
	 *
	 * @param deleteRequest
	 * @return
	 */
	@PostMapping("/delete")
	@ApiOperation("通过id解散队伍")
	public BaseResponse<Boolean> deleteTeam(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
		if (deleteRequest == null || deleteRequest.getId() <= 0) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR);
		}
		// 获取要删除的id（前端传过来的json，会映射到这个deleteRequest属性中
		long id = deleteRequest.getId();

		User loginUser = userService.getLoginUser(request);
		boolean result = teamService.deleteTeam(id, loginUser);
		if (!result) {
			// 如果删除失败
			throw new BusinessException(ErrorCode.SYSTEM_ERROR, "删除失败");
		}
		return ResultUtils.success(true);

	}

	/**
	 * 获取我创建的队伍
	 *
	 * @param teamQueryDTO
	 * @param request
	 * @return
	 */
	@GetMapping("/list/myCreateTeam")
	@ApiOperation("获取我创建的队伍")
	// VO 是用于呈现视图（View）的对象。它通常用于控制器层（Controller）向 前端 页面传递数据。
	// 这里的TeamUserVO有点类似于 脱敏之后的 Team实体类 ，所以专门用于返回给前端
	// 这里不用@Params注解，默认就是这个，如果传过来参数，那么会将 参数对应的映射的实体属性上
	public BaseResponse<List<TeamUserVO>> listCreateMyTeam(TeamQueryDTO teamQueryDTO, HttpServletRequest request) {
		if (teamQueryDTO == null) {
			throw new BusinessException(ErrorCode.NULL_ERROR);
		}

		// 获取登录的用户id，这样查询team表中的userId字段，查对应的记录条数，就可以获取其 创建了几个队伍
		User loginUser = userService.getLoginUser(request);
		long userId = loginUser.getId();
		// 存入到DTO中, 它会在service层中：queryWrapper.eq("userId", userId);，自动构建sql的
		teamQueryDTO.setUserId(userId);
		// （就是 team实体类的 脱敏版）
		// 复用service层接口，（这里设置为true，表示：自己创建的队伍，自己就是有权限修改，不需要管理员权限
		List<TeamUserVO> teamList = teamService.listTeams(teamQueryDTO, true);
		return ResultUtils.success(teamList);
	}

	/**
	 * 获取我加入的队伍
	 *
	 * @param teamQueryDTO
	 * @param request
	 * @return
	 */
	@GetMapping("/list/myJoinTeam")
	@ApiOperation("获取我加入的队伍")
	// VO 是用于呈现视图（View）的对象。它通常用于控制器层（Controller）向 前端 页面传递数据。
	// 这里的TeamUserVO有点类似于 脱敏之后的 Team实体类 ，所以专门用于返回给前端
	// 这里不用@Params注解，默认就是这个，如果传过来参数，那么会将 参数对应的映射的实体属性上
	public BaseResponse<List<TeamUserVO>> listMyJoinTeam(TeamQueryDTO teamQueryDTO, HttpServletRequest request) {
		if (teamQueryDTO == null) {
			throw new BusinessException(ErrorCode.NULL_ERROR);
		}

		// 获取登录的用户id，这样查询team表中的userId字段，查对应的记录条数，就可以获取其 创建了几个队伍
		User loginUser = userService.getLoginUser(request);

		// 构建条件，用于查询user-team表，一个用户加入的  队伍 列表
		QueryWrapper<UserTeam> queryWrapper = new QueryWrapper<>();
		queryWrapper.eq("userId", loginUser.getId());
		List<UserTeam> userTeamList = userTeamService.list(queryWrapper);
		// 取出不重复的 队伍id
		// teamId userId
		// 1, 2
		// 1, 3
		// 2, 3
		// 分组后的结果result
		// 1 => 2, 3
		// 2 => 3
		// Collectors.groupingBy(UserTeam::getTeamId) 是一个流操作，
		// 它将 userTeamList 中的元素按照 UserTeam 对象的 teamId 属性进行分组。
		// 其中键是 teamId，值是具有相同 teamId 的 UserTeam 对象的列表。
		// Collector收集器就是：将集合元素 收集到不同的数据结构中。
		// Collectors 是一个工具类，用于创建收集器（Collector）
		Map<Long, List<UserTeam>> listMap =
				userTeamList.stream().collect(Collectors.groupingBy(UserTeam::getTeamId));

		// 拿到map集合的所有键(teamId) ，然后转换成ArrayList集合
		// 分组之后，就可以 取出不重复的 队伍id（因为相同id的为一组了）
		// 取出队伍id列表
		ArrayList<Long> teamIdList = new ArrayList<>(listMap.keySet());
		// 设置了这个属性，我们记得在service里面，将这个属性作为查询条件，写对应的程序sql
		teamQueryDTO.setTeamIdList(teamIdList);
		// （就是 team实体类的 脱敏版）
		// 复用service层接口，（这里设置为true，表示：自己创建的队伍，自己就是有权限修改，不需要管理员权限
		List<TeamUserVO> teamList = teamService.listTeams(teamQueryDTO, true);
		return ResultUtils.success(teamList);
	}


}
