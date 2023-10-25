package top.niumacoder.userservice.dto.req;

import lombok.Data;

/**
 * 用户请求短信参数
 */
@Data
public class UserMessageReqDTO {
    private String phone;
}
