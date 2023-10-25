package top.niumacoder.userservice.common.constants;

/**
 * Redis Key前缀定义常量
 */
public final class RedisKeyConstants {
    /**
     * 用户注册锁：前缀 + 用户手机号
     */
    public static final String LOCK_USER_REGISTER = "qiniu-user-service:lock:user-register";

    /**
     * 用户注销锁
     */
    public static final String USER_DELETION = "qiniu-user-service:lock:user-deletion";
}
