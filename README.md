# 伙伴匹配系统笔记

## 前端项目初始化

1. 使用vite初始化项目，根据你的需求选择相应地选项

   ```
   npm create vite
   ```

2. 安装依赖

   ```
   npm install
   ```

3. 按需整合vant组件库

4. ```
   npm i vite-plugin-style-import@1.4.1 -D
   ```

5. 关闭vite语法检查（前提使用build启动项目），在package.json文件中，将原始的build改为build": "vite build"

6. 添加axios库

   ```
   npm install axios
   或者
   yarn add axios
   ```

7. 引入vue-router组件，使用yarn add vue-router@4，如果报错请先删除node_modules和yarn.lock文件，再去执行命令

   ```
   yarn add vue-router@4
   ```

   main.ts:

   ```
   import { createApp } from 'vue'
   import App from './App.vue'
   // 1. 引入你需要的组件
   import {Button, Icon, NavBar, Tabbar, TabbarItem, Tag, Divider, TreeSelect, Row, Col, Cell, CellGroup, Form, Field } from 'vant';
   // 2. 引入组件样式
   import * as VueRouter from 'vue-router';
   import routes from './components/config/route.ts';
   
   const app= createApp(App)
   app.use(Button);
   app.use(NavBar);
   app.use(Icon);
   app.use(Tabbar);
   app.use(TabbarItem);
   app.use(Tag);
   app.use(Divider);
   app.use(TreeSelect);
   app.use(Row);
   app.use(Col);
   app.use(Cell);
   app.use(CellGroup);
   app.use(Form);
   app.use(Field);
   app.use(Button);
   
   const router = VueRouter.createRouter({
       // 4. 内部提供了 history 模式的实现。为了简单起见，我们在这里使用 hash 模式。
       history: VueRouter.createWebHashHistory(),
       routes, // `routes: routes` 的缩写
   })
   
   app.use(router);
   app.mount('#app')
   ```

   src/components/config/route.ts:

   ```
   import Index from '../pages/Index.vue';
   import Team from '../pages/TeamPage.vue';
   import User from '../pages/UserPage.vue';
   import SearchPage from '../pages/SearchPage.vue';
   import UserEditPage from "../pages/UserEditPage.vue";
   import SearchResultPage from "../pages/SearchResultPage.vue";
   import UserLoginPage from "../pages/UserLoginPage.vue";
   
   const routes = [
       { path: '/', component: Index },
       { path: '/team', component: Team },
       { path: '/user', component: User },
       { path: '/search', component: SearchPage },
       { path: '/user/list', component: SearchResultPage },
       { path: '/user/edit', component: UserEditPage },
       { path: '/user/login', component: UserLoginPage },
   ]
   
   export default routes;
   ```

## 后端配置

### 后端的全部依赖：

```
    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-redis</artifactId>
            <version>2.7.4</version>
        </dependency>
        <dependency>
            <groupId>org.springframework.session</groupId>
            <artifactId>spring-session-data-redis</artifactId>
            <version>2.7.4</version>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-devtools</artifactId>
            <scope>runtime</scope>
            <optional>true</optional>
        </dependency>
        <dependency>
            <groupId>com.mysql</groupId>
            <artifactId>mysql-connector-j</artifactId>
            <scope>runtime</scope>
        </dependency>
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
        <!-- https://mvnrepository.com/artifact/com.baomidou/mybatis-plus-boot-starter -->
        <dependency>
            <groupId>com.baomidou</groupId>
            <artifactId>mybatis-plus-boot-starter</artifactId>
            <version>3.5.2</version>
        </dependency>

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-configuration-processor</artifactId>
            <optional>true</optional>
        </dependency>
        <dependency>
            <groupId>org.apache.commons</groupId>
            <artifactId>commons-lang3</artifactId>
            <version>3.12.0</version>
        </dependency>
        <dependency>
            <groupId>com.google.code.gson</groupId>
            <artifactId>gson</artifactId>
            <version>2.9.0</version>
        </dependency>
        <dependency>
            <groupId>junit</groupId>
            <artifactId>junit</artifactId>
            <version>4.13.2</version>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>com.github.xiaoymin</groupId>
            <artifactId>knife4j-spring-boot-starter</artifactId>
            <version>2.0.9</version>
        </dependency>
        <!-- https://mvnrepository.com/artifact/com.alibaba/easyexcel -->
        <dependency>
            <groupId>com.alibaba</groupId>
            <artifactId>easyexcel</artifactId>
            <version>3.3.2</version>
        </dependency>
    </dependencies>
```

### application.yml文件：

```
spring:
  mvc:
    pathmatch:
      matching-strategy: ant_path_matcher
  application:
    name: user-center
  profiles:
    active: dev
  #DataSource config
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://localhost:3306/hjj
    username: root
    password: 123456
  #session失效时间（分钟）
  session:
    timeout: 86400
    store-type: redis
  redis:
    port: 6379
    host: localhost
    database: 0
server:
  port: 8080
  servlet:
    context-path: /api
    session:
      cookie:
#        domain: localhost
        same-site: none
        secure: true
#禁止将驼峰转为下划线
mybatis-plus:
  configuration:
    map-underscore-to-camel-case: false
#    mybatis-plus打印日志信息
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl
  global-config:
    db-config:
      logic-delete-field: isDelete # 全局逻辑删除的实体字段名(since 3.3.0,配置后可以忽略不配置步骤2)
      logic-delete-value: 1 # 逻辑已删除值(默认为 1)
      logic-not-delete-value: 0 # 逻辑未删除值(默认为 0)

```

1. ### Redis相关配置

   1. redis依赖

      ```
              <dependency>
                  <groupId>org.springframework.boot</groupId>
                  <artifactId>spring-boot-starter-data-redis</artifactId>
                  <version>2.7.4</version>
              </dependency>
      ```

   2. spring - session redis（自动将session存入redis中）

      ```
              <dependency>
                  <groupId>org.springframework.session</groupId>
                  <artifactId>spring-session-data-redis</artifactId>
                  <version>2.7.4</version>
              </dependency>
      ```

   3. 修改spring-session存储配置 spring.session.store-type默认为none，表示存在单机服务器

      store-type: redis，表示从redis读写session

      ```
      server:
        port: 8080
        servlet:
          context-path: /api
          session:
            cookie:
      #        domain: localhost
              same-site: none
              secure: true
      ```

2. ### 整合Swagger/knife4j接口文档

   #### 目标

   1. 后端整合Swagger + knife4j接口文档
   2. 存量用户信息导入及同步（爬虫）
   3. 前后端联调：搜索页面、用户信息页、用户信息修改页
   4. 标签内容整理
   5. 部分细节优化

   #### 后端

   Swaggere/knife4j接口文档（本项目使用了knife4j）

   Swagger/knife4j原理：

   1. 自定义Swagger配置类

      ```
      import com.github.xiaoymin.knife4j.spring.annotations.EnableKnife4j;
      import org.springframework.context.annotation.Bean;
      import org.springframework.context.annotation.Configuration;
      import springfox.documentation.builders.ApiInfoBuilder;
      import springfox.documentation.builders.PathSelectors;
      import springfox.documentation.builders.RequestHandlerSelectors;
      import springfox.documentation.service.ApiInfo;
      import springfox.documentation.service.Contact;
      import springfox.documentation.spi.DocumentationType;
      import springfox.documentation.spring.web.plugins.Docket;
      import springfox.documentation.swagger2.annotations.EnableSwagger2WebMvc;
      
      @Configuration
      @EnableSwagger2WebMvc
      @EnableKnife4j
      public class Knife4jConfig {
      
          @Bean
          public Docket docket() {
              return new Docket(DocumentationType.SWAGGER_2)
                      .apiInfo(apiInfo())
                      .select()
                      .apis(RequestHandlerSelectors.basePackage("com.hjj.homieMatching.controller"))
                      .paths(PathSelectors.any())
                      .build();
          }
      
          private ApiInfo apiInfo() {
              return new ApiInfoBuilder()
                      .title("文档标题")
                      .description("文档描述")
                      .termsOfServiceUrl("服务条款地址")
                      .version("文档版本")
                      .license("开源版本号")
                      .licenseUrl("开源地址")
                      .contact(new Contact("作者名", "作者网址", "作者邮箱"))
                      .build();
          }
      
      }
      
      
      ```

   2. 定义需要生成文档接口的位置（controller）千万注意：在线上环境不要把接口暴露出去，可以在Swagger配置类添加@Profile({"dev","test"})注解

   3. 启动即可

   4. 若Spring Boot版本大于2.6，需在yml文件中要更换Spring Boot的路径匹配策略或者在Spring Boot启动类中添加@EnableWebMvc注解（不推荐使用，因为还是需要更换匹配策略）

      ```
      spring:
          mvc:
              pathmatch:
                matching-strategy: ant_path_matcher
      ```

   5. 引入对应依赖（Swagger或者Knife4j）

      ```Java
              <dependency>
                  <groupId>io.springfox</groupId>
                  <artifactId>springfox-boot-starter</artifactId>
                  <version>3.0.0</version>
              </dependency>
              <dependency>
                  <groupId>io.springfox</groupId>
                  <artifactId>springfox-swagger-ui</artifactId>
                  <version>3.0.0</version>
              </dependency>
              <dependency>
                  <groupId>com.github.xiaoymin</groupId>
                  <artifactId>knife4j-spring-boot-starter</artifactId>
                  <version>2.0.9</version>
              </dependency>
      ```

   

## 前端遇到的问题

1. ### Toast报错，那是因为视频中用的是vant3，你可能用的是vant4。直接把Toast替换为showSuccessToast，在引入一下就好了。

   引用：

   ```
   import { showSuccessToast } from 'vant';
   ```

   使用：

   ```
   const onChange = (index) => showSuccessToast(`标签 ${index}`); 
   ```

2. ### 如果出现组件在中间的问题，请把style.css文件给删除

3. ### 前端向后端发送请求出现400

   一般4开头的错误码是由于客户端发送请求的有问题，很明显我们在第三期发请求出现的400是由于前端传参出现了问题

   1. 请求参数错误
   2. 前后端请求不一致
   3. 服务器端错误

   解决办法

   先安装qs库，并且要引入qs

   ```
   npm install qs
   或者
   yarn add qs
   ```

   修改后的代码：

   SearchResultPage.vue:

   ```
   <template>
     <user-card-list :user-list="userList" />
     <van-empty v-show="!userList || userList.length < 1" description="暂无符合要求的用户" />
   </template>
   
   <script setup>
   import {useRoute} from "vue-router";
   import {onMounted, ref} from "vue";
   import myAxios from "../../plugins/myAxios.ts";
   import qs from 'qs';
   import UserCardList from "../UserCardList.vue";
   
   const route = useRoute();
   const { tags } = route.query;
   
   const userList = ref([]);
   
   onMounted(async() => {
     const userListData = await myAxios.get('/user/search/tags', {
       params: {
         tagNameList: tags
       },
       paramsSerializer: params => {
         return qs.stringify(params, { indices: false })
       }
     }).then(function (response) {
       console.log('/user/search/tags succeed', response);
       return response?.data;
     }).catch(function(error) {
       console.log('/user/search/tags error', error)
     })
     if(userListData) {
       userListData.forEach(user => {
         if(user.tags){
           user.tags = JSON.parse(user.tags);
         }
       })
       // 如果请求成功，就把响应结果返回给userList
       userList.value = userListData;
     }
   })
   
   </script>
   
   <style scoped>
   
   </style>
   ```

   myAxios.ts:

   ```
   import axios from 'axios';
   
   const myAxios = axios.create({
       baseURL: 'http://localhost:8080/api'
   })
   
   myAxios.defaults.withCredentials = true; //设置为true
   
   // Add a request interceptor
   myAxios.interceptors.request.use(function (config) {
       console.log('我要发请求啦')
       return config;
   }, function (error) {
       return Promise.reject(error);
   });
   
   myAxios.interceptors.response.use(function (response) {
       console.log('我收到你的响应啦')
       return response.data;
   }, function (error) {
       // Do something with response error
       return Promise.reject(error);
   });
   
   export default myAxios;
   ```

4. ### 前端报错 import myAxios from "../../plugins/myAxios.ts";、

   引入文件时去掉拓展名就好了

   比如：

   ```
   import myAxios from "../../plugins/myAxios.ts";
   改为
   import myAxios from "../../plugins/myAxios";
   ```

5. ### Axios发送请求获取data失败

   因为Axios自动帮我们封装了一层data，所以我们取的时候要多取一层data

   myAxios.ts:

   ```
   import axios from 'axios';
   
   const myAxios = axios.create({
       baseURL: 'http://localhost:8080/api'
   })
   
   myAxios.defaults.withCredentials = true; //设置为true
   
   // Add a request interceptor
   myAxios.interceptors.request.use(function (config) {
       console.log('我要发请求啦')
       return config;
   }, function (error) {
       return Promise.reject(error);
   });
   
   myAxios.interceptors.response.use(function (response) {
       console.log('我收到你的响应啦')
       return response.data;
   }, function (error) {
       // Do something with response error
       return Promise.reject(error);
   });
   
   export default myAxios;
   ```

6. ### 前端发送登录请求没有携带Cookie

   在myAxios.ts文件中添加

   ```
       myAxios.defaults..withCredentials = true;//向后端发送请求携带cookie
   ```

   如果添加了上面的代码还是无效的话可以试试下面的两个方法（可以都试试）

   可以在后端common包中添加一个解决**跨域配置类，**这样的话就好了哈

   CorsConfig:

   ```
   import org.springframework.context.annotation.Configuration;
   import org.springframework.web.servlet.config.annotation.CorsRegistry;
   import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
   
   /*
    * 解决跨域问题：重写WebMvcConfigurer的addCorsMappings方法（全局跨域配置）
    * @author rabbiter
    * @date 2023/1/3 1:30
    */
   @Configuration
   public class CorsConfig implements WebMvcConfigurer {
    
       @Override
       public void addCorsMappings(CorsRegistry registry) {
           registry.addMapping("/**")
                   //是否发送Cookie
                   .allowCredentials(true)
                   //放行哪些原始域
                   .allowedOriginPatterns("*")
                   .allowedMethods(new String[]{"GET", "POST", "PUT", "DELETE"})
                   .allowedHeaders("*")
                   .exposedHeaders("*");
       }
   }
   ```

   除此之外呢还可以看看这位鱼友的帖子：https://articles.zsxq.com/id_2v7g78iofjn7.html

7. ### Uncaught (in promise) TypeError: Cannot read properties of undefined (reading 'username') 出现了user.username undefined

   可以看看这位鱼友的帖子，https://wx.zsxq.com/dweb2/index/topic_detail/814245214541452

8. ### 出现 $setup.user.createTime.toISOString is not a function问题

   ```
   把
   <van-cell title="注册时间" is-link to="/user/edit" :value="user.createTime.toISOString()" />
   改为
   <van-cell title="注册时间" is-link to="/user/edit" :value="user?.createTime" /> 试试
   ```

9. ### 没有任何的报错，但是个人信息就是显示不出来

   如果没有其他的报错的，但是还是显示不出来用户的信息，可试试下面的方法

   ```
       把
         <template if="user">
       <van-cell title="昵称" is-link to="/user/edit" :value="user?.username"/>
       <van-cell title="账号" is-link to="/user/edit" :value="user?.userAccount" />
       <van-cell title="头像" is-link to="/user/edit">
         <img style="height: 48px" :src="user?.avatarUrl"/>
       </van-cell>
       <van-cell title="性别" is-link to="/user/edit" :value="user?.gender" @click="toEdit('gender', '性别', user.gender)"/>
       <van-cell title="电话" is-link to="/user/edit" :value="user?.phone" @click="toEdit('phone', '电话', user.phone)"/>
       <van-cell title="邮箱" is-link to="/user/edit" :value="user?.email" />
       <van-cell title="星球编号" is-link to="/user/edit" :value="user?.planetCode" />
       <van-cell title="注册时间" is-link to="/user/edit" :value="user.createTime" />
     </template>
     
       换成 
       
       <van-cell title="昵称" is-link to="/user/edit" :value="user?.username"/>
       <van-cell title="账号" is-link to="/user/edit" :value="user?.userAccount" />
       <van-cell title="头像" is-link to="/user/edit">
         <img style="height: 48px" :src="user?.avatarUrl"/>
       </van-cell>
       <van-cell title="性别" is-link to="/user/edit" :value="user?.gender" @click="toEdit('gender', '性别', user.gender)"/>
       <van-cell title="电话" is-link to="/user/edit" :value="user?.phone" @click="toEdit('phone', '电话', user.phone)"/>
       <van-cell title="邮箱" is-link to="/user/edit" :value="user?.email" />
       <van-cell title="星球编号" is-link to="/user/edit" :value="user?.planetCode" />
   <!--    <van-cell title="注册时间" is-link to="/user/edit" :value="user.createTime" />-->
   
   ```

10. 前端DateTimePicker时间选择器报错显示不出来，可能你用的是vant4，而vant4中DateTimePicker组件已被启用，改为了DatePicker组件‘

   main.ts引入

   ```
import { createApp } from 'vue';
import { Form, Field, CellGroup } from 'vant';

const app = createApp();
app.use(Form);
app.use(Field);
app.use(CellGroup);

   ```

   TeamAddPage.vue的对应代码

   ```
      <van-field
          v-model="result"
          is-link
          readonly
          name="datePicker"
          label="时间选择"
          placeholder="点击选择时间"
          @click="showPicker = true"
      />
      <van-popup v-model:show="showPicker" position="bottom">
        <van-date-picker @confirm="onConfirm" @cancel="showPicker = false" />
      </van-popup>
   ```

   

## 后端遇到的问题

1. ### Mybatis-Plus分页查询查不出来？或者报错？

   1. 'Page' 为 abstract；无法实例化或者'com.baomidou.mybatisplus.extension.service.IService' 中的 'page(com.baomidou.mybatisplus.core.metadata.IPage<com.hjj.homieMatching.model.domain.User>, com.baomidou.mybatisplus.core.conditions.Wrapper<com.hjj.homieMatching.model.domain.User>)' 无法应用于 '(org.springframework.data.domain.Page<com.hjj.homieMatching.model.domain.User>, com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<com.hjj.homieMatching.model.domain.User>)'

      **这是因为Page的包引错了，而第二个错误是因为类型不兼容**

      应该引的是

      ```
      import com.baomidou.mybatisplus.core.metadata.IPage;
      import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
      // 对应的controller部分的代码改为
          @GetMapping("/recommend")
          public BaseResponse<IPage<User>> recommendUsers(long pageSize, long pageNum, HttpServletRequest request){
              QueryWrapper<User> queryWrapper = new QueryWrapper<>();
              IPage<User> page = new Page<>(pageNum, pageSize);
              IPage<User> userList = userService.page(page, queryWrapper);
              return ResultUtils.success(userList);
          }
      ```

2. ### 如果出现无法解析符号 'MybatisPlusInterceptor'

   package com.hjj.homieMatching.config;

   import com.baomidou.mybatisplus.annotation.DbType;
   import com.baomidou.mybatisplus.autoconfigure.ConfigurationCustomizer;
   import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
   import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
   import org.springframework.context.annotation.Bean;
   import org.springframework.context.annotation.Configuration;

   **这个问题说明你的Mybatis-Plus依赖版本过低，请调整为3.4.0以上。**

3. ### 配置MyBatis-Plus分页查询配置类

   ```
   import com.baomidou.mybatisplus.annotation.DbType;
   import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
   import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
   import org.springframework.context.annotation.Bean;
   import org.springframework.context.annotation.Configuration;
   
   @Configuration
   public class MybatisPlusConfig {
   
       /**
        * 新的分页插件,一缓和二缓遵循mybatis的规则,需要设置 MybatisConfiguration#useDeprecatedExecutor = false 避免缓存出现问题(该属性会在旧插件移除后一同移除)
        */
       @Bean
       public MybatisPlusInterceptor mybatisPlusInterceptor() {
           MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
           PaginationInnerInterceptor paginationInnerInterceptor = new PaginationInnerInterceptor();
           paginationInnerInterceptor.setDbType(DbType.MYSQL);
           paginationInnerInterceptor.setOverflow(true);
           interceptor.addInnerInterceptor(paginationInnerInterceptor);
           return interceptor;
       }
   
   }
   ```

4. ### 利用RedisTemplate往Redis中存值，发现乱码

   为什么在这里我们往redis存值取值发现是存在的，但是在redis客户端取值却显示不存在呢？

   因为redistemplate帮我们序列化了，。这就是为什么鱼皮哥在Redis GUI软件查看键是乱码的

   ![](C:\Users\17653\AppData\Roaming\Typora\typora-user-images\image-20231226182455021.png)

   

   想用RedisTemplate往Redis中进行crud操作时，得提前配置一下

   ```
   import org.springframework.context.annotation.Bean;
   import org.springframework.context.annotation.Configuration;
   import org.springframework.data.redis.connection.RedisConnectionFactory;
   import org.springframework.data.redis.core.RedisTemplate;
   import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
   import org.springframework.data.redis.serializer.StringRedisSerializer;
   
   @Configuration
   public class RedisConfig {
       @Bean
       public RedisTemplate<String,Object> redisTemplate(RedisConnectionFactory connectionFactory){
           RedisTemplate<String, Object> template=new RedisTemplate<>();
           template.setConnectionFactory(connectionFactory);
           template.setKeySerializer(new StringRedisSerializer());
           template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
           return template;
       }
   }
   
   ```

5. updateById()传入的实体类必须有id字段，我就是以为没有传入id字段，导致队长转交不出去，出现了bug

6. 后端listTeam接口查不到过期时间迟于当前时间的队伍，可能是你的queryWrapper未初始化，并且它包含了上面的查询条件，多重查询条件叠加在一起就很难查到了，建议把queryWrapper给初始化一下。

   ```
           // 不展示已过期的队伍
           // expireTime为空或者过期时间迟于当前时间
           queryWrapper = new QueryWrapper<>();
           queryWrapper.and(qw -> qw.gt("expireTime", new Date()).or().isNull("expireTime"));
           List<Team> teamList = this.list(queryWrapper);
           if (CollectionUtils.isEmpty(teamList)) {
               return new ArrayList<>();
           }
   ```

   


## 爬虫

### 存量用户信息导入

1. 把所有星球用户的信息导入
2. 把写了自我介绍的同学的标签信息导入

### 怎么抓取网上信息

1. 分析原网站是怎么获取这些数据的？哪个接口？按 F 12 打开控制台，查看网络请求，复制 curl 代码便于查看和执行
2. **用程序去调用接口** （java okhttp httpclient / python 都可以）
3. 处理（清洗）一下数据，之后就可以写到数据库里

#### 具体流程

1. 从 excel 中导入全量用户数据，**判重** 。 easy excel：https://alibaba-easyexcel.github.io/index.html
2. 抓取写了自我介绍的同学信息，提取出用户昵称、用户唯一 id、自我介绍信息
3. 从自我介绍中提取信息，然后写入到数据库中

##### EasyExcel

两种读对象的方式：

1. 确定表头：建立对象，和表头形成映射关系
2. 不确定表头：每一行数据映射为 Map<String, Object>

两种读取模式：

1. 监听器：先创建监听器、在读取文件时绑定监听器。单独抽离处理逻辑，代码清晰易于维护；一条一条处理，适用于数据量大的场景。
   TableListener:

   ```
   import com.alibaba.excel.context.AnalysisContext;
   import com.alibaba.excel.metadata.data.ReadCellData;
   import com.alibaba.excel.read.listener.ReadListener;
   import lombok.extern.slf4j.Slf4j;
   
   import java.util.Map;
   
   // 有个很重要的点 TableListener 不能被spring管理，要每次读取excel都要new,然后里面用到spring可以构造方法传进去
   @Slf4j
   public class TableListener implements ReadListener<PlanetUserInfo> {
   
       @Override
       public void invoke(PlanetUserInfo planetUserInfo, AnalysisContext analysisContext) {
   
       }
       @Override
       public void doAfterAllAnalysed(AnalysisContext context) {
   
       }
       @Override
       public void invokeHead(Map<Integer, ReadCellData<?>> headMap, AnalysisContext context) {
           ReadListener.super.invokeHead(headMap, context);
       }
   }
   ```

   ImportExcel:

   ```
   import com.alibaba.excel.EasyExcel;
   
   /**
    * 导入excel数据
    */
   public class ImportExcel {
       /**
        * 读取数据
        */
       public static void main(String[] args) {
           // 写法1：JDK8+ ,不用额外写一个DemoDataListener
           // since: 3.0.0-beta1
           String fileName = "E:\\Java星球项目\\homieMatching\\homieMatching\\src\\main\\resources\\alarm.csv";
           // 这里默认每次会读取100条数据 然后返回过来 直接调用使用数据就行
           // 具体需要返回多少行可以在`PageReadListener`的构造函数设置
           EasyExcel.read(fileName, PlanetUserInfo.class, new TableListener()).sheet().doRead();
       }
   }
   
   ```

   PlanetUserInfo:

   ```
   import com.alibaba.excel.annotation.ExcelProperty;
   
   public class PlanetUserInfo {
       @ExcelProperty("ID")
       private String ID;
       @ExcelProperty("alarm")
       private String alarm;
   }
   ```

   

2. 同步读：无需创建监听器，一次性获取完整数据。方便简单，但是数据量大时会有等待时常，也可能内存溢出。

   ```
       public static void synchronousRead(String fileName){
           // 这里 需要指定读用哪个class去读，然后读取第一个sheet 同步读取会自动finish
           List<PlanetUserInfo> totalDataList = EasyExcel.read(fileName).head(PlanetUserInfo.class).sheet().doReadSync();
       }
   ```

   



## 干货

### router.push()和router.replace()的区别

router.push()会在原有的历史记录中压入新的历史记录，而router.place()则会代替上一条历史记录，这样用户点击返回不会回到登录页了

### Session共享

种 session 的时候注意范围，cookie.domain

如果想要共享cookie，可以种一个更高层的域名，将要共享session的两个域名设为二级域名

### 如何开启同一后端项目，但配置不同的端口号

```
java -jar .\homieMatching-0.0.1-SNAPSHOT.jar --server.port=8081
```



### 为什么在服务器A登录后，服务器B拿不到用户信息

因为用户在A登录，session只存在于A中，而B中没有，所以服务器B获取用户信息时会失败

#### 解决办法

1. Redis（基于内存的 K/V 数据库）

   将Cookie存储在Redis中实现分布式登录，在A中登录的Cookie存在Redis中，那么B要获取登录信息先从Redis中获取对应Cookie再拿到登录信息

2. MySQL

3. 文件服务器ceph

### 导入数据

1. 用户可视化界面：适合一次性导入，数据量可控

2. 写程序：for循环，建议分批，不要一次梭哈（可以用接口控制）**要保证可控，幂等，注意线上环境和测试环境是有区别的**

   导入1000万条，for i 1000w

#### 编写一次性任务

for循环插入数据的问题：

1. 建立和释放数据库的链接（批量插入）
2. for循环是绝对线性的（并发），并发要注意执行的先后顺序，不要使用非并发类的集合

```
//CPU密集型：分配的核心线程数=CPU-1
//I0密集型：分配的核心线程数可以大于CPU核数
```

数据库慢？预先把数据查出来，放到一个更快读取的地方，不用再
查数据库了。（缓存）
预加载缓存，定时更新缓存。（定时任务）
多个机器都要执行任务么？（分布式锁：控制同一时间只有一台机
器去执行定时任务，其他机器不用重复执行了)

### 数据查询慢怎么办？

用缓存：提前把数据取出来保存好（通常保存在读写更快地介质，比如内存），就可以更快地读写

缓存的实现：

- Redis（分布式缓存，支持多个进程或者多个服务器之间的数据共享）
- memcached（分布式）
- Etcd（主要用于共享配置和服务发现。云原生架构的一个分布式，扩容能力强）
- ehcache（单机）
- 本地缓存（Java的Map集合）
- Caffeine（是一个Java库，但是呢它是本地的，只能在单个JVM进程中使用，不能再多个进程或服务器之间共享数据）
- Google Guava

### router的params传参和query传参的区别

- **`params`（路径参数）：** 这些参数直接嵌入在URL路径中。它们是路径的一部分，因此可以包含各种类型的数据，包括对象。由于它们是直接嵌入在 URL 中的，所以有一些限制，例如不能包含特殊字符或空格。在Vue Router中，这些参数以冒号（`:`）的形式定义在路由路径中。**params**需要与**name**结合，params更类似于我们平常所说的post请求方式。
- **`query`（查询参数）：** 这些参数附加在URL的末尾，以键值对的形式出现，并以`?`开始。查询参数通常用于过滤、排序或包含非路径关联的数据。由于它们是作为字符串附加到URL的，需要进行URL编码，而URL编码后的字符串通常不方便传递包含复杂结构的对象。**query**需要与**path**结合，query更类似于我们平常所说的get请求方式

### @click.stop

如果你想要在点击按钮时阻止父元素的点击事件，可以使用`.stop`修饰符，但是需要注意这是全局的阻止事件冒泡，可能会影响其他地方的事件传播。（可以运用在点击队伍详情页。当点击加入队伍时就不会跳转队伍详情页了。）



## Redis

- 基于内存的K/V存储中间件
- NoSQL键值对数据库
- 也可作为消息队列

### Java中操作Redis的方式

1. Spring Data Redis（推荐）

   通用的数据库访问框架，定义了一组**增删改查**的接口

2. Jedis（独立于Spring操作Redis）

3. Redisson

#### Jedis

- 独立于Spring操作Redis的Java客户端
- 要配合Jedis Pool使用
- **<u>Jedis与commons pool会有冲突</u>**

#### Lettuce

**高阶**的操作Redis的Java客户端

- 支持异步、连接池

#### Redisson（写在简历上，是个亮点）

分布式操作Redis的Java客户端，像操作本地的集合一样操作Redis

#### JetCache



### 操作方式对比：

1. 如果你用Spring开发，并且没有过多的定制化要求选Spring Data Redis
2. 如果你没有用Spring，并且追求简单，没有过多的性能要求，可以用Jedis + Jedis Pool
3. 如果你的项目不是Spring，并且追求高性能，高定制化，可以用lettuce。支持异步、连接池（技术大牛使用）
4. 如果你的项目是分布式的，需要用到一些分布特性（比如分布式锁，分布式集合），推荐使用Redisson



### Redis数据结构

- String类型：sex:"男"

- List列表：hobby:["编程","睡觉"]

- Set集合：hobby:["编程","睡觉"]，值不重复。可用于点赞，即一个人只能点一次赞

- Hash哈希：nameAge:{"burger":1,"hamburger":2}

- Zset集合：相比对Set多一个score分数，是一个有顺序的Set集合，一般用作实现排行榜

- bloomfilter（布隆过滤器，主要从大量数据中快速过滤值，比如邮件黑名单拦截）

- geo（计算地理位置）

- hyperloglog（pv / uv）

- pub / sub （发布订阅，类似消息队列）

- BitMap（101101011110101001）可用于签到和存储压缩值

  

### 如何如何设计缓存Key

目的：使得不同用户看到的数据不同

systemId:moudleId:func:options(不要和别人冲突)

homie:user:recommend:

redis 内存不能无限增加，k一定要设置过期时间



### **缓存预热**

问题：第一个用户访问还是很慢（加入第一个勇士），也能一定程度上保护数据库

缓存预热的优缺点：

1. 解决上述问题，让用户始终访问很快

缺点：

1. 增加开发成本（需要额外的开发和设计）
2. 预热的时机和时间不合适的话，有可能你缓存的数据不对或者太老
3. 空间换时间

#### 缓存预热目的

用定时任务，每天刷新所有用户的推荐列表

注意点：

1. 缓存预热的意义(新增少、总用户多)
2. 缓存的空间不能太大，要预留给其他缓存空间
3. 缓存数据的周期

#### 怎么预热缓存？

1. 定时触发（常用）
2. 手动触发

#### 定时任务实现

1. Spring Scheduler（Spring Boot默认整合的）
2. Quartz（独立于Spring Boot存在的定时任务框架）
3. XXL-Job之类的分布式任务调度平台（界面 + SDK）



第一种方式：

1. 在主类添加@EnableScheduling注解
2. 给要执行的方法添加@Scheduling注解，指定cron表达式或者执行频率

不要去背cron表达式



### 缓存穿透

用户访问的数据既不在缓存也不再数据库中，导致大致请求到达数据库，对数据库造成巨大压力

### 缓存击穿

大量的Key同时失效或者Redis宕机，导致大量请求访问数据库，带来巨大压力

### 缓存雪崩

也叫热点Key问题。在一段时间内，被高并发访问并且缓存重建业务较为复杂的Key突然失效，巨量的请求会抵达数据库，对其造成巨大冲击



### 控制定时任务的发布

#### why？

1. 浪费资源，想象10000条服务器同时“打鸣”
2. 脏数据，比如重复插入

要控制定时任务在同一时间只有一个服务器执行

#### 实现方式：

1. 分离定时任务，只安排一个服务器执行定时任务。成本太大

2. 写死配置，每个服务器都执行定时任务，但是只有IP地址符合配置的服务器才会执行。适合于并发量不大的场景，成本低。问题：IP可能是不固定的。

3. 动态配置，配置是可以轻松得、方便更新得，但还是只有ip符合配置的服务器才真会执行业务逻辑代码

   - 数据库
   - Redis
   - 配置中心（Nacos，Apollo，Spring Cloud Config）

   问题：服务器多了，IP不可控还是很麻烦，还要人工修改

4. 分布式锁，只有抢到锁的服务器才能执行对应的业务逻辑。

   - 坏处：增加成本
   - 好处：不用手动配置，不管有多少个服务器在抢锁

​	

**单机就会存在故障**

### 锁

在资源有限的情况下，控制同一时间（段）只有某些线程（用户 / 服务器）能够访问资源

Java实现锁：synchronized，并发包



#### 分布式锁

为啥需要分布式锁？

1. 从锁的必要性出发。在资源有限的情况下，控制同一时间（段）只有某些线程（用户 / 服务器）能够访问资源
2. 单个锁只对单个JVM有效

#### 分布式锁实现的关键

##### 抢锁机制

怎么保证同一时间只有一个服务器能抢到锁？

核心思想：先来的人先把数据改为自己独有的标识（比如服务器IP），后来的人发现标识存在，则抢锁失败，继续等到。等先来的人的执行方法结束，把标识清空，其他人继续抢锁。

实现方式：

1. MySQL数据库：select for update行级锁（最简单）

2. 乐观锁

3. **Redis实现**：内存数据库，速度快。支持setnx，lua脚本支持原子性操作

   setnx: set if not exists如果不存在，则设置；只有设置成功才会返回true

4. Zookeeper实现（不推荐）

##### 注意事项

1. 用完就释放锁（腾地方）√

2. 锁一定要添加过期时间，防止因为服务器宕机没释放锁 √

3. 如果方法执行时间过长，锁提前过期？

   问题：

   1. 连锁效应：释放别人的锁
   2. 这样还是会存在多个方法同时执行的情况

   解决方案：

   - 续期

     如何判断方法为执行完？

     Aop实现：提前定义flag为false，如果执行完毕呢flag则设为true，未执行完成依旧是false。通过循环线程，判断flag是否为true，false表示未执行完，true表示执行完了。

     ![image-20231227215621095](C:\Users\17653\AppData\Roaming\Typora\typora-user-images\image-20231227215621095.png)

4. 释放锁的时候，有可能先判断出来是自己的锁，但key提前过期，最后还是释放了别人的锁。同时B抢到资源执行业务方法，并释放了锁，这时呢C又进来了，执行了业务方法并释放了锁。![image-20231227215649910](C:\Users\17653\AppData\Roaming\Typora\typora-user-images\image-20231227215649910.png)

   解决方案：Redis + lua脚本实现，可理解为一个事务

   

#### Redisson实现分布式锁

Java客户端，数据网格

实现了很多Java里支持的接口和数据结构



Redisson是一个Java操作Redis的客户端，**提供了大量的分布式数据集来简化对Redis的操作和使用，可以让开发者像使用本地集合一样使用Redis，完全感受不到Redis的存在。**



##### 2种引入方式

1. spring boot starter引入（不推荐，版本迭代太快，容易冲突）htps:业github.com/redisson/redisson/tree/master/redisson-spring-boot-starter
2. 直接引入：https://github.com/redisson/redisson#quick-start

示例代码：

```
// 数据存在 redis 的内存中
        RList<Object> rList = redissonClient.getList("test-list");
        System.out.println("rList:" + rList.get(0));
        rList.remove(0);
        // map
        Map<String, Integer> map = new HashMap();
        map.put("yupi", 10);
        map.get("yupi");

        RMap<String, Integer> rMap = redissonClient.getMap("test-map");
        rMap.put("yupi", 10);
        rMap.get("yupi");
```



##### 引入Redisson依赖：

```
        <dependency>
            <groupId>org.redisson</groupId>
            <artifactId>redisson</artifactId>
            <version>3.17.5</version>
        </dependency>
```

##### 编写Redisson配置类

```
import lombok.Data;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "spring.redis")
@Data
public class RedissonConfig {
    private String host;
    private String port;

    @Bean
    public RedissonClient redissonClient(){
        // 1. 创建配置
        Config config = new Config();
        String redisAddress = String.format("redis://%s:%s", host, port);
        config.useSingleServer().setAddress(redisAddress).setDatabase(3);//设置单个服务器，设置地址，选择数据库
        // 2. 创建势力
        RedissonClient redisson = Redisson.create(config);
        return redisson;
    }
}
```



##### 定时任务 + 锁

1. waitTime设置为0，只抢一次，抢不到就放弃
2. 注意释放锁要写在finally中

##### 实现代码

```
    public void watchDogTest(){
        String doCacheLockId = String.format("%s:precachejob:docache:lock", RedisConstant.SYSTEM_ID);
        RLock lock = redissonClient.getLock(doCacheLockId);
        try {
            // 只有一个线程能够获取锁
            if (lock.tryLock(0, -1, TimeUnit.MILLISECONDS)) {
                // to do
                doSomething // 业务代码
                System.out.println(Thread.currentThread().getId() + "我拿到锁了");
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally { // 不管所是否会失效都会执行下段保证释放锁
            // 只能释放自己的锁
            if (lock.isHeldByCurrentThread()) { // 判断当前的锁是不是当前这个线程加的锁，每次抢锁时都会有一个线程Id，
                // 这个Id会存在redis中，验证线程的id就好了
                System.out.println(Thread.currentThread().getId() + "锁已经释放了");
                lock.unlock(); // 执行业务逻辑后，要释放锁
            }
        }
    }
```



##### 看门狗机制

> Redisson帮我们实现了续期机制

可以理解为给你要执行的方法添加监听器，当你方法未执行完会进行续期，重置Redis锁的过期时间

原理：

1. 监听当前线程，每十秒续期一次，过期时长为30s（补到30s）
2. 如果线程挂掉（**注意**：debug模式也会被它当成服务器宕机），则不会续期

##### 为什么看门狗能够自动续期还得手动释放呢，另外续期时间为30s，而不是永久？

怕宕机，防止Redis宕机导致锁未释放



Redisson 分布式锁的watch dog自动续期机制：https://blog.csdn.net/qq_26222859/article/details/79645203

##### 看门狗失效原因：

程序debug导致watch dog认为redis宕机，从而失效了



**分布式锁导致其他服务器数据不一致**

使用红锁（redlock）





## 组队功能

理解为王者荣耀的组队



#### 理想的应用场景

我要跟别人一起参加竞赛或者做项目，可以发起队伍或者加入别人队伍



### 需求分析

用户可以**创建**一个队伍，设置队伍的人数，队伍名称（标题）、描述、超时时间

> 队长、剩余的人数
>
> 聊天？
>
> 公开或私密或加密
>
> 信息流不展示已过期的队伍
>
> 根据标签或者名称搜索队伍

展示队伍信息表，根据标签或者名称搜索队伍 P0

修改队伍信息 P 0~P1

用户创建队伍**最多5个**

用户可以加入队伍（其他人、未满、未过期），允许加入多个队伍，但有个上限 P0

> 是否需要队长同意？筛选审批

用户可以退出队伍（如果队长退出，队长顺位，把权限给新来的）P1

队长可以解散队伍 P0



----



邀请其他用户加入队伍，分享队伍 P1

队伍人满后发送消息通知 P1



### 系统（接口）设计

1. 创建队伍

   用户可以**创建**一个队伍，设置队伍的人数，队伍名称（标题）、描述、超时时间

   > 队长、剩余的人数
   >
   > 聊天？
   >
   > 公开或私密或加密
   >
   > 信息流不展示已过期的队伍
   >
   > 根据标签或者名称搜索队伍

   1. 请求参数	是否为空

   2. 是否登录，未登录不允许创建

   3. 校验信息

      1. 队伍人数 > 1，且 <= 20
      2. 队伍标题 <= 20
      3. 描述 <= 512
      4. status是否公开（int）不传默认为0（公开）
      5. 如果status是加密状态，一定要有密码，且密码 <= 10
      6. 超时时间 > 当前时间
      7. 校验用户只能创建5个队伍

   4. 插入队伍信息到队伍表

   5. 插入用户 => 队伍关系到关系表

      （注意：最后两个操作得同时进行，要么都不执行，要么都执行，是原子操作。）

      实现方式：

      **事务注解**

      ```
      @Transactional(rollbackFor = Exception.class)
      ```

      要么SQL语句都执行成功，要么都不成功。即当用户创建队伍时，要将对应的信息插入team表和user_team表，要么都插入，要么都不插入，这时候可以通过事务来解决。

2. 查询队伍列表

   分页展示队伍列表，根据名称、最大人数等搜索队伍  P0，信息流中不展示已过期的队伍

   1. 从请求参数中取出队伍名称等查询条件，如果存在则作为查询条件
   2. 不展示已过期的队伍（根据过期时间筛选）
   3. 可以通过某个**关键词**同时对名称和描述查询
   4. **只有管理员才能查看加密还有非公开的房间**
   5. 关联查询已加入队伍的用户信息
   6. **关联查询已加入队伍的用户信息（可能会很耗费性能，建议大家用自己写 SQL 的方式实现）** **todo**

   实现方式

   1. 自己写SQL语句

      ```
      // 查询队伍和创建人的信息
      // select * from team t left join user u on t.userId = u.id
      
      // 查询队伍和已加入队伍的用户信息
      // select * from team t joint user_team ut on t.id = ut.teamId left join user u on ut.userId = u.id
      ```

   2. 用Mybatis-Plus

      

3. 修改队伍信息

   1. 判断请求参数是否为空

   2. 查询队伍是否存在

   3. 只有管理员或者队长可以修改

   4. 如果用户传入的新值和老值一致，就不用update了
      (可自行实现，降低数据库使用次数)

   5. 如果队伍是公开的，则密码不可修改

   6. 更新成功

      

4. 用户可以加入队伍

   **其他人、未满、未过期，允许加入多个队伍，但是要有个上限 P0**

   1. 用户最多加入5个队伍
   2. 只能加入未满、未过期的队伍
   3. 不能重复加入已加入的队伍（幂等性）
   4. 禁止加入私有队伍
   5. 如果加入队伍是加密的，必须密码匹配才能加入
   6. 新增队伍-用户关联信息

5. 用户可以退出队伍

   > 如果队长退出，权限转移给第二早加入的用户——先来后到，并且删除关系表中的数据

   请求参数：队伍id

   1. 校验请求参数

   2. 校验队伍是否存在

   3. 校验我是否已加入队伍

   4. 如果队伍

      1. 只剩一人，队伍解散

      2. 还有其他人

         1. 如果是队长退出队伍，队长转移给第二早加入队伍的人——先来后到

            > 取id最小的两条数据

         2. 非队长，自己退出队伍

6. 队长可以解散队伍

   请求参数：队伍Id

   1. 校验请求参数
   2. 校验队伍是否存在
   3. 校验你是不是队长
   4. 移除所有加入队伍的关联信息
   5. 删除队伍

7. 获取当前用户已加入的队伍

8. 获取当前用户创建的队伍

   复用listTeam方法，只增加查询条件，不修改代码

分享队伍 =》 邀请其他用户加入队伍 P1

业务流程：

1. 生成分享链接
2. 用户访问链接，可以点击加入

**在写校验数据的业务代码时，最好把访问数据库的逻辑代码放在后面，减少数据库的压力**

**注意，一定要加上事务注解，保证操作的原子性**

#### 库表设计

队伍表team （注意使用插件生成代码时，要将isDelete字段机上@TableLogic注解）

字段：

- id主键 bigint（最简单，连续，放在url上较简短，但易被爬虫）

- teamName 队伍名称

- description 描述

- maxNum 最大人数

- expireTime 过期时间

- userId 队伍创建者id

- status 0 - 公开， 1 - 私有，2 - 加密

- password 密码

- createTime 创建时间

- updateTime 修改时间

- isDelete 是否删除

  

```sql
create table if not exists hjj.team
(
    id           bigint auto_increment comment 'id'
        primary key,
    teamName   		varchar(256)                        not null comment '队伍名称',
    description 	varchar(1024)                       null comment ' 描述',
    maxNum        	int    default 1              		null comment '最大人数',
    expireTime      datetime							null comment '过期时间',
    userId 			bigint                              not null comment '队伍创建者id',
    status         	tinyint default 0 		        	null comment '队伍状态 - 0 - 公开， 1 - 私有，2 - 加密
- ',
    password        varchar(512)                       null comment '队伍密码',
    createTime   	datetime default CURRENT_TIMESTAMP null comment '创建时间',
    updateTime   	datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete     	tinyint  default 0                 not null comment '是否删除',
)
    comment '队伍信息';
```



用户-队伍表 user-team（注意使用插件生成代码时，要将isDelete字段机上@TableLogic注解）

字段：

- id 主键
- userId 用户id
- teamId 队伍Id
- joinTime 加入时间
- createTime 创建时间
- updateTime 修改时间
- isDelete 是否删除



```sql
create table if not exists hjj.user_team
(
    id           bigint auto_increment comment 'id'
        primary key,
    userId 			bigint                             	comment '用户id',
    teamId 			bigint                             	comment '队伍id',
    joinTime   		datetime 							 comment '加入时间',
    createTime   	datetime default CURRENT_TIMESTAMP null comment '创建时间',
    updateTime   	datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete     	tinyint  default 0                 not null comment '是否删除',
)
    comment '用户队伍关系表';

```



两个关系：

1. 用户加了那些队伍？
2. 队伍有哪些用户

方式：

1. **建立用户-队伍关系表**teamId userId（适用于并发量高的场景，对查询性能有一定要求。不用全表遍历，只需要维护一个关系）
2. 用户表补充已加入的队伍字段，队伍表补充已加入的用户字段（代码少，通过队伍查用户，通过用户查队伍）

### 增删改查

业务逻辑开发（P0）



### 为什么需要请求参数包装类？

1. 请求参数名称 / 类型和实体类不一样
2. 有一些参数用不到，如果要生成接口文档会增加理解成本
3. 多个字段映射到同一个对象

### 为什么需要包装类？

1. 可能有些字段需要隐藏，不能返回给前端
2. 有些字段的某些方法是不关心的

### 引入依赖beanutils

```
<!-- https://mvnrepository.com/artifact/commons-beanutils/commons-beanutils -->
<dependency>
    <groupId>commons-beanutils</groupId>
    <artifactId>commons-beanutils</artifactId>
    <version>1.9.4</version>
</dependency>
```



## 随机匹配

> 为了帮大家更快地发现和自己兴趣相同的朋友

匹配 1 个还是匹配多个？

答：匹配多个，并且按照匹配的相似度从高到低排序



怎么匹配？（根据什么匹配）

答：标签 tags

> 还可以根据 user_team 匹配加入相同队伍的用户



本质：找到有相似标签的用户

举例：

用户 A：[Java, 大一, 男]

用户 B：[Java, 大二, 男]

用户 C：[Python, 大二, 女]

用户 D：[Java, 大一, 女]



#### 1. 怎么匹配



1. 找到有共同标签最多的用户（TopN）
2. 共同标签越多，分数越高，越排在前面
3. 如果没有匹配的用户，随机推荐几个（降级方案）

编辑距离算法：https://blog.csdn.net/DBC_121/article/details/104198838

最小编辑距离：字符串 1 通过最少多少次增删改字符的操作可以变成字符串 2

余弦相似度算法：[https://blog.csdn.net/m0_55613022/article/details/125683937](https://blog.csdn.net/m0_55613022/article/details/125683937)（如果需要带权重计算，比如学什么方向最重要，性别相对次要）



#### 2. 怎么对所有用户匹配，取	 TOP

直接取出所有用户，依次和当前用户计算分数，取 TOP N（54 秒）
优化方法：

1. 切忌不要在数据量大的时候循环输出日志（取消掉日志后 20 秒）
2. Map 存了所有的分数信息，占用内存解决：维护一个固定长度的有序集合（sortedSet），只保留分数最高的几个用户（时间换空间）e.g.【3, 4, 5, 6, 7】取 TOP 5，id 为 1 的用户就不用放进去了
3. 细节：剔除自己
4. 1. 根据部分标签取用户（前提是能区分出来哪个标签比较重要）
   2. 只查需要的数据（比如 id 和 tags） √（7.0s）
5. 提前查？（定时任务）
   1. 提前把所有用户给缓存（不适用于经常更新的数据）
   2. 提前运算出来结果，缓存（针对一些重点用户，提前缓存）

大数据推荐，比如说有几亿个商品，难道要查出来所有的商品？
难道要对所有的数据计算一遍相似度？
检索 => 召回 => 粗排 => 精排 => 重排序等等
检索：尽可能多地查符合要求的数据（比如按记录查）
召回：查询可能要用到的数据（不做运算）
粗排：粗略排序，简单地运算（运算相对轻量）
精排：精细排序，确定固定排位



## 分表学习建议

mycat、sharding sphere框架

一致性hash



## 队伍操作按钮权限控制

加入队伍：仅非创建人且未加入队伍可见

更新队伍：仅创建人可见

解散退伍：仅创建人可见

退出队伍：仅加入队伍的人可见



## todo&&待优化

1. 分布式锁导致的其他服务器数据不统一的问题或者多个Redis里的数据不一致。

2. 强制登录，自动跳转到登录页面 √

   解决：axios全局配置响应拦截，并且添加重定向

3. 用户重复加入队伍（如果用户在短时间内重复发送请求，则一个队伍会加入了若干个相同用户）

   加分布式锁 => 一个锁锁用户 => 防止用户同时加入10个不同的队伍，达到用户加入队伍的上线

   ​			  一个锁锁队伍 => 防止同一个用户加入同一个队伍n次

4. 标签

5. 前端全局响应拦截，自动跳转登录页 √

6. 前端死标题 √

   使用route.beforeEach，根据要跳转页面的url路径 匹配config/routes配置的title字段

7. 前端缺少展示已加入人数 √

8. 前端status改为枚举值 √

9. 加入加密的房间需要密码 √

10. 仅加入队伍和创建队伍的人能看到队伍操作按钮（listTeam接口要能获取我加入的队伍状态）√

    1. 前端查询我加入了哪些队伍，然后判断每个队伍id是否在列表中（前端要多发一次请求）
    2. 在后端做上述事情（推荐）

11. 前端拦截器统一拦截输出日志 √



## 如何改造成小程序

Cordova，uniapp



## 上线

先区分多环境，前端区分开发和线上接口，后端prod改为用线上公网可以访问的数据库

前端：Vercel(免费)
https://vercel.com/
后端：微信云托管（部署容器的平台，付费）
https://cloud.weixin.qq.com/cloudrun/service
