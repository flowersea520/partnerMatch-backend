package com.lxc.partnerMatch.model.request;

import lombok.Data;

import java.io.Serializable;

/**
 * 退出队伍的请求DTO，只要队伍id
 *
 * @author mortal
 * @date 2024/4/28 14:57
 */
@Data
public class TeamQuitRequest implements Serializable {


	private static final long serialVersionUID = -1394480681912760757L;
	/**
	 * 队伍id
	 */
	private Long teamId;


}
