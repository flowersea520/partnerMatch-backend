package com.lxc.usercenter.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2WebMvc;

/**
 *  自定义Knife4j接口文档的配置
 *  配置类 Knife4jConfiguration 中并没有直接使用 Knife4j 的注解和类，而是使用了 Springfox Swagger 的相关注解和类。
 *  具体来说，你使用了 @EnableSwagger2WebMvc 注解来启用 Swagger2 的支持，并且使用了 Docket 类来配置 Swagger2 的文档生成。
 */
@Configuration
@EnableSwagger2WebMvc
//可以在 Spring 的配置类中使用 @Profile 注解来标注不同的配置
/**
 *  千万注意：线上环境prod千万不要把接口暴露出去！！，可以通过SwaggerConfig或者knife4j配置文件中的开头加上
 *  @Profile({"dev", "test"}) 这个注解：仅限于在 开发环境和测试环境用这个配置
 */
@Profile({"dev", "test"})
public class Knife4jConfiguration {
    @Bean(value = "defaultApi2")
    public Docket defaultApi2() {
        return new Docket(DocumentationType.SWAGGER_2)  // DocumentationType.SWAGGER_2 固定的，代表swagger2
//                .groupName("分布式任务系统") // 如果配置多个文档的时候，那么需要配置groupName来分组标识
                .apiInfo(apiInfo()) // 用于生成API信息
                .select() // select()函数返回一个ApiSelectorBuilder实例,用来控制接口被swagger做成文档
                // 这里一定要标注你的选择器Controller的位置
                .apis(RequestHandlerSelectors.basePackage("com.lxc.usercenter.controller")) // 用于指定扫描哪个包下的接口
                .paths(PathSelectors.any())// 选择所有的API,如果你想只为部分API生成文档，可以配置这里
                .build();
    }

    /**
     * 用于定义API主界面的信息，比如可以声明所有的API的总标题、描述、版本
     * @return
     */
    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title("lxc用户中心") //  可以用来自定义API的主标题
                .description("lxc用户管理中心接口文档") // 可以用来描述整体的API
                .termsOfServiceUrl("https://github.com/flowersea520") // 用于定义服务的域名
                .version("1.0") // 可以用来定义版本。
                .build(); //
    }


}
