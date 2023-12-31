package top.niumacoder.userservice.dto.req;

import lombok.Data;

/**
 * 用户注销请求参数
 */
@Data
public class UserDeletionReqDTO {
    /**
     * 手机号
     */
    private String phone;
}
