package top.niumacoder.userservice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import top.niumacoder.frameworks.convention.result.Result;
import top.niumacoder.frameworks.web.Results;
import top.niumacoder.userservice.dto.req.UserLoginReqDTO;
import top.niumacoder.userservice.dto.req.UserMessageReqDTO;
import top.niumacoder.userservice.dto.req.UserRegisterReqDTO;
import top.niumacoder.userservice.dto.resp.UserLoginRespDTO;
import top.niumacoder.userservice.dto.resp.UserRegisterRespDTO;
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

    @PostMapping("/user-service/api/loginByPasswrod")
    public Result<UserLoginRespDTO> loginByPassword(@RequestBody UserLoginReqDTO requestParam) {
        UserLoginRespDTO result = userLoginService.loginByPassword(requestParam);
        return Results.success(result);
    }

    @PostMapping("/user-service/api/loginByCode")
    public Result<UserLoginRespDTO> loginByCode(@RequestBody UserLoginReqDTO requestParam) {
        UserLoginRespDTO result = userLoginService.loginByCode(requestParam);
        return Results.success(result);
    }

    @PostMapping("/user-service/api/register")
    public Result<UserRegisterRespDTO> register(@RequestBody UserRegisterReqDTO requestParam) {
        UserRegisterRespDTO result = userLoginService.register(requestParam);
        return Results.success(result);
    }

    @GetMapping("/user-service/api/check-login")
    public Result<UserLoginRespDTO> checkLogin(@RequestParam("accessToken") String accessToken) {
        UserLoginRespDTO result = userLoginService.checkLogin(accessToken);
        return Results.success(result);
    }

    public Result<Void> logout(@RequestParam(required = false) String accessToken) {
        userLoginService.logout(accessToken);
        return Results.success();
    }
}
