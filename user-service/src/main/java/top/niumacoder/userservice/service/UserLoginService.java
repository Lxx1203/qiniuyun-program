package top.niumacoder.userservice.service;

import top.niumacoder.userservice.dto.req.UserMessageReqDTO;
import top.niumacoder.userservice.dto.req.UserLoginReqDTO;
import top.niumacoder.userservice.dto.req.UserRegisterReqDTO;
import top.niumacoder.userservice.dto.resp.UserLoginRespDTO;
import top.niumacoder.userservice.dto.resp.UserRegisterRespDTO;
import top.niumacoder.userservice.service.impl.UserDeletionReqDTO;

/**
 * 用户登录接口
 */
public interface UserLoginService {
    /**
     * 用户登录接口通过密码
     *
     * @param requestParam 用户登录入参
     * @return 用户登录返回结果
     */
    UserLoginRespDTO loginByPassword(UserLoginReqDTO requestParam);

    /**
     * 用户登录通过验证码
     * @param requestParam-用户登录入参
     * @return
     */
    UserLoginRespDTO loginByCode(UserLoginReqDTO requestParam);

    void sendMsg(UserMessageReqDTO requestParam);
    /**
     * 通过 Token 检查用户是否登录
     *
     * @param accessToken 用户登录 Token 凭证
     * @return 用户是否登录返回结果
     */
    UserLoginRespDTO checkLogin(String accessToken);

    /**
     * 用户退出登录
     *
     * @param accessToken 用户登录 Token 凭证
     */
    void logout(String accessToken);
    
    
    /**
     * 用户注册
     *
     * @param requestParam 用户注册入参
     * @return 用户注册返回结果
     */
    UserRegisterRespDTO register(UserRegisterReqDTO requestParam);

    /**
     * 注销用户
     *
     * @param requestParam 注销用户入参
     */
    void deletion(UserDeletionReqDTO requestParam);
}
