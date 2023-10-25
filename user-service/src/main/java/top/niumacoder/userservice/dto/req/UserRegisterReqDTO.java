package top.niumacoder.userservice.dto.req;

import lombok.Builder;
import lombok.Data;

/**
 * 用户注册请求参数
 */
@Data
@Builder
public class UserRegisterReqDTO {

    /**
     * 用户名
     */
    private String username;

    /**
     * 密码
     */
    private String password;
    
    /**
     * 电话
     */
    private String phone;
}
