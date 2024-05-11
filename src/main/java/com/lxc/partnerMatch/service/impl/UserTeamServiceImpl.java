package com.lxc.partnerMatch.service.impl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lxc.partnerMatch.mapper.UserTeamMapper;
import com.lxc.partnerMatch.model.domain.UserTeam;
import com.lxc.partnerMatch.service.UserTeamService;
import org.springframework.stereotype.Service;

/**
* @author lxc
* @description 针对表【user_team(用户队伍关系表)】的数据库操作Service实现
* @createDate 2024-04-26 01:18:02
*/
@Service
public class UserTeamServiceImpl extends ServiceImpl<UserTeamMapper, UserTeam>
    implements UserTeamService {

}




