package top.niumacoder.userservice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import top.niumacoder.frameworks.convention.result.Result;
import top.niumacoder.frameworks.web.Results;
import top.niumacoder.userservice.dto.req.UserMessageReqDTO;
import top.niumacoder.userservice.service.UserLoginService;

/**
 * 用户登录控制层
 */
@RestController
@RequiredArgsConstructor
public class UserLoginController {
    private final UserLoginService userLoginService;

    @PostMapping("/user-service/api/sendMsg")
    public Result<String> sendMsg(@RequestBody UserMessageReqDTO requestParam) {
        userLoginService.sendMsg(requestParam);
        return Results.success("验证码已发送");
    }
}
