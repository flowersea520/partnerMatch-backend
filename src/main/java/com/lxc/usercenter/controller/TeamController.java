package com.lxc.usercenter.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lxc.usercenter.common.BaseResponse;
import com.lxc.usercenter.common.ErrorCode;
import com.lxc.usercenter.common.ResultUtils;
import com.lxc.usercenter.exception.BusinessException;
import com.lxc.usercenter.model.domain.Team;
import com.lxc.usercenter.model.domain.User;
import com.lxc.usercenter.model.dto.TeamQueryDTO;
import com.lxc.usercenter.model.request.TeamAddRequest;
import com.lxc.usercenter.model.request.TeamJoinRequest;
import com.lxc.usercenter.model.request.TeamUpdateRequest;
import com.lxc.usercenter.model.vo.TeamUserVO;
import com.lxc.usercenter.service.TeamService;
import com.lxc.usercenter.service.UserService;
import org.apache.commons.beanutils.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import java.util.List;

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

	/**
	 * 增加队伍操作，一般是 传过来 实体类，然后返回 添加成功后的记录id作为data传给前端就可以了
	 *
	 * @param teamAddRequest
	 * @return
	 */
	@PostMapping("/add")
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
	 * 删除 队伍， 这个一般是根据传过来的  id去删除， 删除成功后返回一个Boolean类型的值就可以了
	 *
	 * @param id
	 * @return
	 */
	@DeleteMapping("/delete")
	public BaseResponse<Boolean> deleteTeam(@RequestParam long id) {
		// 第一件事情就是非空判断，判断其传过来的数据 是否为空
		if (id < 0) {
			throw new BusinessException(ErrorCode.NULL_ERROR);
		}
		boolean result = teamService.removeById(id);
		if (!result) {
			// 如果删除失败
			throw new BusinessException(ErrorCode.SYSTEM_ERROR, "删除失败");
		}
		return ResultUtils.success(true);

	}

	/**
	 * 更新队伍：根据前端传过来的实体类（修改后的实体类，让我们更新数据库的），
	 * 修改这个数据库后返回给前端一个Boolean值就可以了，表示我们修改成功了
	 *
	 * @param teamUpdateRequest
	 * @return
	 */
	@PutMapping("/update")
	public BaseResponse<Boolean> updateTeam(@RequestBody TeamUpdateRequest teamUpdateRequest, HttpServletRequest request)  {
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
	// VO 是用于呈现视图（View）的对象。它通常用于控制器层（Controller）向 前端 页面传递数据。
	// 这里的TeamUserVO有点类似于 脱敏之后的 Team实体类 ，所以专门用于返回给前端
	public BaseResponse<List<TeamUserVO>> listTeams( TeamQueryDTO teamQueryDTO, HttpServletRequest request) {
		if (teamQueryDTO == null) {
			throw new BusinessException(ErrorCode.NULL_ERROR);
		}
		// 根据传过来的request对象，判断是否是管理员
		boolean isAdmin = userService.isAdmin(request);
// （就是 team实体类的 脱敏版）
		List<TeamUserVO> teamList = teamService.listTeams(teamQueryDTO, isAdmin);
		return ResultUtils.success(teamList);
	}





	/**
	 * 分页查询 队伍，还是传过来一个封装好的DTO（这个DTO专门用来查询队伍的一个实体），返回给前端一个 page对象
	 *
	 * @param teamQueryDTO
	 * @return
	 */
	@GetMapping("/list/page")
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
	 *  加入队伍
	 * @return
	 */
	@PostMapping("/join")
	public BaseResponse<Boolean> joinTeam(@RequestBody TeamJoinRequest teamJoinRequest, HttpServletRequest request) {
		if (teamJoinRequest == null) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR);
		}
		// 调用业务层
		boolean result = teamService.joinTeam(teamJoinRequest, request);
		return ResultUtils.success(result);

	}

}
