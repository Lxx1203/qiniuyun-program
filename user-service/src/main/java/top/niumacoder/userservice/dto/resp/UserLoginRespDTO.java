package top.niumacoder.userservice.dto.resp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户登录返回参数
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserLoginRespDTO {
    /**
     * id
     */
    private String id;

    /**
     * 用户名
     */
    private String username;

    /**
     * 头像
     */
    private String avatar;

    /**
     * Token
     */
    private String accessToken;
}
