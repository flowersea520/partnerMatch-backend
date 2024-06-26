package com.lxc.partnerMatch.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lxc.partnerMatch.model.domain.Team;
import com.lxc.partnerMatch.model.domain.User;
import com.lxc.partnerMatch.model.dto.TeamQueryDTO;
import com.lxc.partnerMatch.model.request.TeamJoinRequest;
import com.lxc.partnerMatch.model.request.TeamQuitRequest;
import com.lxc.partnerMatch.model.request.TeamUpdateRequest;
import com.lxc.partnerMatch.model.vo.TeamUserVO;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
* @author lxc
* @description 针对表【team(队伍表)】的数据库操作Service
* @createDate 2024-04-26 01:15:35
*/
public interface TeamService extends IService<Team> {

	/**
	 *  创建队伍
	 * @param team
	 * @param loginUser
	 * @return
	 */
	long addTeam(Team team, User loginUser);

	/**
	 *  查询队伍列表
	 * @param teamQueryDTO
	 * @return
	 */
	List<TeamUserVO> listTeams(TeamQueryDTO teamQueryDTO, boolean isAdmin);

	/**
	 *  更新队伍
	 * @param teamUpdateRequest
	 * @return
	 */
	boolean updateTeam(TeamUpdateRequest teamUpdateRequest, User loginUser);

	/**
	 *  加入队伍
	 * @param teamJoinRequest
	 * @return
	 */
	boolean joinTeam(TeamJoinRequest teamJoinRequest, HttpServletRequest request);

	/**
	 *  判断传入的 要修改的请求实体队伍，是否和之前的 队伍实体是否一致，一致就不修改
	 * @param teamUpdateRequest
	 * @param oldTeam
	 * @return
	 */
	 boolean isUpdateRequired(TeamUpdateRequest teamUpdateRequest, Team oldTeam);

	/**
	 *  退出队伍
	 * @param teamQuitRequest
	 * @param loginUser
	 * @return
	 */
	boolean quitTeam(TeamQuitRequest teamQuitRequest, User loginUser);

	/**
	 *  解散（删除）队伍
	 * @param id
	 * @return
	 */
	boolean deleteTeam(long id, User loginUser);
}
