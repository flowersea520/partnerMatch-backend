package com.lxc.usercenter;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 启动类
 * *author lxc
 */
@SpringBootApplication
@MapperScan("com.lxc.usercenter.mapper")
// 使用 @EnableScheduling 注解时，Spring 框架会自动扫描带有 @Scheduled 注解的方法，
// 并将其注册为定时任务。@Scheduled 注解用于指定方法何时执行，可以根据时间表（cron 表达式）或固定的延迟/间隔来执行。
@EnableScheduling
public class UserCenterApplication {

    public static void main(String[] args) {
        SpringApplication.run(UserCenterApplication.class, args);
    }

}
