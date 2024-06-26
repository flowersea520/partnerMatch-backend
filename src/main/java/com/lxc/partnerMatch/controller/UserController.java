package com.lxc.partnerMatch.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

import com.lxc.partnerMatch.common.BaseResponse;
import com.lxc.partnerMatch.common.ErrorCode;
import com.lxc.partnerMatch.common.ResultUtils;
import com.lxc.partnerMatch.exception.BusinessException;
import com.lxc.partnerMatch.model.domain.User;
import com.lxc.partnerMatch.model.request.UserLoginRequest;
import com.lxc.partnerMatch.model.request.UserRegisterRequest;
import com.lxc.partnerMatch.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.stream.Collectors;

import static com.lxc.partnerMatch.contant.UserConstant.USER_LOGIN_STATE;
import static javax.swing.UIManager.get;

/**
 * 用户接口
 * author lxc
 */
@RestController
@RequestMapping("/user")
@Slf4j
public class UserController {

	@Resource
	private UserService userService;
	@Resource
	private RedisTemplate redisTemplate;

	/**
	 * 用户注册
	 *
	 * @param userRegisterRequest
	 * @return
	 */
	@PostMapping("/register")
	public BaseResponse<Long> userRegister(@RequestBody UserRegisterRequest userRegisterRequest) {
		// 校验
		if (userRegisterRequest == null) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR);
		}
		String userAccount = userRegisterRequest.getUserAccount();
		String userPassword = userRegisterRequest.getUserPassword();
		String checkPassword = userRegisterRequest.getCheckPassword();
		String planetCode = userRegisterRequest.getPlanetCode();
		if (StringUtils.isAnyBlank(userAccount, userPassword, checkPassword, planetCode)) {
			return null;
		}
		long result = userService.userRegister(userAccount, userPassword, checkPassword, planetCode);
		return ResultUtils.success(result);
	}

	/**
	 * 用户登录
	 *
	 * @param userLoginRequest
	 * @param request
	 * @return
	 */
	@PostMapping("/login")
	public BaseResponse<User> userLogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request) {
		if (userLoginRequest == null) {
			return ResultUtils.error(ErrorCode.PARAMS_ERROR);
		}
		String userAccount = userLoginRequest.getUserAccount();
		String userPassword = userLoginRequest.getUserPassword();
		if (StringUtils.isAnyBlank(userAccount, userPassword)) {
			return ResultUtils.error(ErrorCode.PARAMS_ERROR);
		}
		User user = userService.userLogin(userAccount, userPassword, request);
		return ResultUtils.success(user);
	}

	/**
	 * 用户注销
	 *
	 * @param request
	 * @return
	 */
	@PostMapping("/logout")
	public BaseResponse<Integer> userLogout(HttpServletRequest request) {
		if (request == null) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR);
		}
		int result = userService.userLogout(request);
		return ResultUtils.success(result);
	}

	/**
	 * 获取当前用户
	 *
	 * @param request
	 * @return
	 */
	@GetMapping("/current")
	public BaseResponse<User> getCurrentUser(HttpServletRequest request) {
		Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
		User currentUser = (User) userObj;
		if (currentUser == null) {
			throw new BusinessException(ErrorCode.NOT_LOGIN);
		}
		long userId = currentUser.getId();
		// TODO 校验用户是否合法
		User user = userService.getById(userId);
		User safetyUser = userService.getSafetyUser(user);
		return ResultUtils.success(safetyUser);
	}

	@GetMapping("/search")
	public BaseResponse<List<User>> searchUsers(String username, HttpServletRequest request) {
		if (!userService.isAdmin(request)) {
			throw new BusinessException(ErrorCode.NO_AUTH, "缺少管理员权限");
		}
		QueryWrapper<User> queryWrapper = new QueryWrapper<>();
		if (StringUtils.isNotBlank(username)) {
			queryWrapper.like("username", username);
		}
		List<User> userList = userService.list(queryWrapper);
		List<User> list = userList.stream().map(user -> userService.getSafetyUser(user)).collect(Collectors.toList());
		return ResultUtils.success(list);
	}

	/**
	 * 根据用户标签进行搜索用户
	 *
	 * @param tagNameList
	 * @return
	 */
	@GetMapping("/search/tags")
	public BaseResponse<List<User>> searchUserByTags(@RequestParam(required = false) List<String> tagNameList) {
		if (CollectionUtils.isEmpty(tagNameList)) {
			throw new BusinessException(ErrorCode.NULL_ERROR);
		}
		List<User> userList = userService.searchUsersByTags(tagNameList);
		return ResultUtils.success(userList);
	}

	/**
	 * 在index页面查看 推荐用户
	 *
	 * @param  request
	 * @param pageNum  pageNum表示要获取的页码
	 * @param pageSize pageSize表示每页的用户数量
	 *                 注意：(配置了分页插件后：传入的 Page 对象中已经包含了页码和每页大小的信息,MyBatis Plus 会根据这些信息 自动 计算出偏移量，
	 *                 并将其用于分页查询。因此，你不需要手动进行 (pageNum - 1) * pageSize 这样的计算。）
	 * @return
	 */
	@GetMapping("/recommend")
	public BaseResponse<Page<User>> recommendUsers(@RequestParam long pageNum, long pageSize, HttpServletRequest request) {
		// 调用业务层  -- 获取分页对象
		Page<User> userPage = userService.getUserPage(pageNum, pageSize, request);
		return ResultUtils.success(userPage);
	}


	/**
	 * 更新用户信息
	 *
	 * @param user 注意：如果前端传过来{gender: 男}这个json对象，那么这里会将其变成Java的user对象，然后user实体的其他的属性默认为null
	 * @return
	 * @RequestBody 注解：会将请求体中的JSON或其他格式的数据转换成User的Java对象
	 */
	@PostMapping("/update")
  	public BaseResponse<Integer> updateUser(@RequestBody User user, HttpServletRequest request) {
		// 1. 校验参数是否为空
		if (user == null) {
			return new BaseResponse<>(ErrorCode.NULL_ERROR);
		}
		User loginUser = userService.getLoginUser(request);
		// 3. 触发更新
		Integer result = userService.updateUser(user, loginUser);
		return ResultUtils.success(result);

	}


	@PostMapping("/delete")
	public BaseResponse<Boolean> deleteUser(@RequestParam long id, HttpServletRequest request) {
		if (!userService.isAdmin(request)) {
			throw new BusinessException(ErrorCode.NO_AUTH);
		}
		if (id <= 0) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR);
		}
		boolean b = userService.removeById(id);
		return ResultUtils.success(b);
	}


	/**
	 *  根据登录的 用户，去匹配和其相似的 其他用户
	 * @param num
	 * @param request
	 * @return
	 */
	@GetMapping("match")
	// 这里用vo，一定要返回前端脱敏后的对象列表
	public BaseResponse<List<User>> matchUsers(long num, HttpServletRequest request) {
		// 只能让用户最多 匹配20个
		if (num <= 0 || num > 20) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR, "最多匹配20个相似用户");
		}
		// 获取登录用户（如果获取loginUser的tags一直为null，那么先清除缓存，它会优先读取缓存里面的内容）
		User loginUser = userService.getLoginUser(request);
//		如果service层在某些方法中使用了缓存（service层中的getUserPage方法），
//		那么调用了这些方法的Controller层方法可能会受到缓存的影响。
		// 根据登录的 用户，去匹配和其相似的 其他用户
		// 这里在controller封装好 要返回给前端的 相应内容（service层就是返回data，
		// 我们这里将data封装到 BaseResponse里面 -- 这样有code和message了
		return ResultUtils.success(userService.matchUsers(num, loginUser));
	}


}
