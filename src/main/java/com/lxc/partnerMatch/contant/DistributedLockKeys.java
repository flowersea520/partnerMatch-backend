package com.lxc.partnerMatch.contant;

/**
 *  分布式锁 对象 常量类
 */
public class DistributedLockKeys {
	//		锁对象：通常并不直接存储对象数据，而是通过控制对某个资源或代码段的访问权限来保证并发安全性。
	public static final String PARTNER_MATCH_CACHE_LOCK_KEY = "partnerMatch:preCachejob:doCache:lock";

	public static final String PARTNER_MATCH_JOINTEAM_LOCK_KEY = "partnerMatch:join_team:lock";
}