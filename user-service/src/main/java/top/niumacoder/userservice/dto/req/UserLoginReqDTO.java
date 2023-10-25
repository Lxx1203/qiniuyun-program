package top.niumacoder.userservice.dto.req;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 *用户登录请求参数
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserLoginReqDTO {
    /**
     * 用户名
     */
    private String usernameOrPhone;

    /**
     * 密码
     */
    private String password;
    
    /**
     * 验证码
     */
    private String code;
}