package com.lxc.partnerMatch.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
	@Override
	public void addCorsMappings(CorsRegistry registry) {
		registry.addMapping("/**")
//				记得不同的服务，详细到端口号
				.allowedOrigins("http://39.101.78.159", "http://localhost:8000",
						"http://localhost:5173", "http://localhost:8001", "\n" +
								"http://localhost:3000", "http://www.flowersea.site",
						"http://localhost:3000", "https://39.101.78.159") // 允许这些源访问
				.allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // 允许的方法
				.allowedHeaders("*") // 允许的头部
				.allowCredentials(true) // 是否允许携带凭证
				.maxAge(3600); // 预请求缓存的时间长度，单位秒
	}
}