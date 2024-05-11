package com.lxc.partnerMatch.service;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lxc.partnerMatch.model.domain.User;
import com.baomidou.mybatisplus.extension.service.IService;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
/**
 * 用户服务  (通用的东西，写到service里面）
 * *author lxc
 */
public interface UserService extends IService<User> {

    /**
     * 用户注册
     *
     * @param userAccount   用户账户
     * @param userPassword  用户密码
     * @param checkPassword 校验密码
     * @param planetCode    星球编号
     * @return 新用户 id
     */
    long userRegister(String userAccount, String userPassword, String checkPassword, String planetCode);

    /**
     * 用户登录
     *
     * @param userAccount  用户账户
     * @param userPassword 用户密码
     * @param request
     * @return 脱敏后的用户信息
     */
    User userLogin(String userAccount, String userPassword, HttpServletRequest request);

    /**
     * 用户脱敏
     *
     * @param originUser
     * @return
     */
    User getSafetyUser(User originUser);

    /**
     * 用户注销
     *
     * @param request
     * @return
     */
    int userLogout(HttpServletRequest request);

    /**
     *  根据标签搜索用户
     * @param tagNameList 用户传递过来的标签列表
     * @return
     */
    List<User> searchUsersByTags(List<String> tagNameList);

    /**
     *  更新用户信息
     * @param user
     * @return
     */
    int updateUser(User user, User loginUser);


    /**
     * 获取当前登录的用户信息
     * @param request
     * @return
     */
    User getLoginUser(HttpServletRequest request);

    /**
     * 根据传递过来的登录态中的用户  判断 是否为管理员
     *
     * @param request
     * @return
     */
   boolean isAdmin(HttpServletRequest request);

    /**
     *  判断登录对象是否为管理员
     * @param loginUser
     * @return
     */
    boolean isAdmin(User loginUser);

    /**
     * 在index页面查看 推荐用户（分页）
     * @param pageNum
     * @param pageSize
     * @return
     */
    Page<User> getUserPage(long pageNum, long pageSize, HttpServletRequest request);

    /**
     * 根据登录的 用户，去匹配和其相似的 其他用户
     * @param num
     * @param loginUser
     * @return
     */
    List<User> matchUsers(long num, User loginUser);



}
