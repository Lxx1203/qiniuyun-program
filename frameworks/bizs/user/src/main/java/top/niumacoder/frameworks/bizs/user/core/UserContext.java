package top.niumacoder.frameworks.bizs.user.core;

import com.alibaba.ttl.TransmittableThreadLocal;

import java.util.Optional;

/**
 * 用户上下文
 */
public final class UserContext {
    private static final ThreadLocal<UserInfoDTO> USER_THREAD_LOCAL = new TransmittableThreadLocal<>();

    /**
     * 设置用户上下文
     *
     * @param user
     */
    public static void setUser(UserInfoDTO user) {
        USER_THREAD_LOCAL.set(user);
    }

    /**
     * 获取用户id
     *
     * @return 用户ID
     */
    public static String getUserId() {
        return Optional.ofNullable(USER_THREAD_LOCAL.get()).map(UserInfoDTO::getUserId).orElse(null);
    }

    /**
     * 获取用户名
     *
     * @return 用户名
     */
    public static String getUserName() {
        return Optional.ofNullable(USER_THREAD_LOCAL.get()).map(UserInfoDTO::getUsername).orElse(null);
    }

    /**
     * 获取用户Token
     *
     * @return 用户TOken
     */
    public static String getToken() {
        return Optional.ofNullable(USER_THREAD_LOCAL.get()).map(UserInfoDTO::getToken).orElse(null);
    }
    /**
     * 获取用户Token
     *
     * @return 用户TOken
     */
    public static String getPhone() {
        return Optional.ofNullable(USER_THREAD_LOCAL.get()).map(UserInfoDTO::getPhone).orElse(null);
    }
    /**
     * 清理用户上下文
     *
     */
    public static void removeUser() {
        USER_THREAD_LOCAL.remove();
    }
}
