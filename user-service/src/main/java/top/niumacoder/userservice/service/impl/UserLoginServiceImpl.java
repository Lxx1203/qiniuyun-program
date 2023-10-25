package top.niumacoder.userservice.service.impl;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.niumacoder.frameworks.bizs.user.core.UserContext;
import top.niumacoder.frameworks.bizs.user.core.UserInfoDTO;
import top.niumacoder.frameworks.bizs.user.toolkit.JWTUtil;
import top.niumacoder.frameworks.cache.DistributedCache;
import top.niumacoder.frameworks.common.toolkit.BeanUtil;
import top.niumacoder.frameworks.convention.expection.ClientException;
import top.niumacoder.frameworks.convention.expection.ServiceException;
import top.niumacoder.frameworks.designpattern.chain.AbstractChainContext;
import top.niumacoder.userservice.dao.entity.UserDO;
import top.niumacoder.userservice.dao.entity.UserPhoneDO;
import top.niumacoder.userservice.dao.mapper.UserMapper;
import top.niumacoder.userservice.dao.mapper.UserPhoneMapper;
import top.niumacoder.userservice.dto.req.UserDeletionReqDTO;
import top.niumacoder.userservice.dto.req.UserMessageReqDTO;
import top.niumacoder.userservice.dto.req.UserLoginReqDTO;
import top.niumacoder.userservice.dto.req.UserRegisterReqDTO;
import top.niumacoder.userservice.dto.resp.UserLoginRespDTO;
import top.niumacoder.userservice.dto.resp.UserRegisterRespDTO;
import top.niumacoder.userservice.service.UserLoginService;
import top.niumacoder.userservice.toolkit.ValidateCodeUtils;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static top.niumacoder.userservice.common.constants.RedisKeyConstants.LOCK_USER_REGISTER;
import static top.niumacoder.userservice.common.constants.RedisKeyConstants.USER_DELETION;
import static top.niumacoder.userservice.common.constants.UserConstant.USER_REGISTER_DEFAULT_AVATAR_URL;
import static top.niumacoder.userservice.common.constants.UserConstant.USER_REGISTER_DEFAULT_USERNAME_PREFIX;
import static top.niumacoder.userservice.common.enums.UserChainMarkEnum.USER_REGISTER_FILTER;
import static top.niumacoder.userservice.common.enums.UserRegisterErrorCodeEnum.PHONE_REGISTERED;
import static top.niumacoder.userservice.common.enums.UserRegisterErrorCodeEnum.USER_REGISTER_FAIL;

/**
 * 用户登录接口实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserLoginServiceImpl implements UserLoginService {
    private final UserMapper userMapper;
    private final UserPhoneMapper userPhoneMapper;
    private final DistributedCache distributedCache;
    private final AbstractChainContext<UserRegisterReqDTO> abstractChainContext;
    private final RedissonClient redissonClient;

    @Override
    public UserLoginRespDTO loginByPassword(UserLoginReqDTO requestParam) {
        String usernameOrPhone = requestParam.getUsernameOrPhone();

        String username;
        //判断是否为手机号
        LambdaQueryWrapper<UserPhoneDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserPhoneDO::getPhone, usernameOrPhone);
        username = Optional.ofNullable(userPhoneMapper.selectOne(queryWrapper))
                .map(UserPhoneDO::getUsername)
                .orElse(null);
        username = Optional.ofNullable(username).orElse(usernameOrPhone);

        LambdaQueryWrapper<UserDO> userDoQueryWrapper = new LambdaQueryWrapper<>();
        userDoQueryWrapper.eq(UserDO::getUsername, username)
                .eq(UserDO::getPassword, requestParam.getPassword())
                .select(UserDO::getId, UserDO::getUsername, UserDO::getAvatar);
        UserDO userDO = userMapper.selectOne(userDoQueryWrapper);
        if (userDO != null) {
            UserInfoDTO userInfo = UserInfoDTO.builder()
                    .userId(String.valueOf(userDO.getId()))
                    .username(userDO.getUsername())
                    .build();
            String accessToken = JWTUtil.generateAccessToken(userInfo);
            UserLoginRespDTO userLoginRespDTO = new UserLoginRespDTO(userInfo.getUserId(), userInfo.getUsername(), userDO.getAvatar(), accessToken);
            distributedCache.put(accessToken, JSON.toJSONString(userLoginRespDTO), 3, TimeUnit.DAYS);
            return userLoginRespDTO;
        }
        throw new ClientException("账号不存在或密码错误");
    }


    @Override
    public void sendMsg(UserMessageReqDTO requestParam) {
        String phone = requestParam.getPhone();
        LambdaQueryWrapper<UserPhoneDO> userPhoneDOLambdaQueryWrapper = new LambdaQueryWrapper<>();
        userPhoneDOLambdaQueryWrapper.eq(UserPhoneDO::getPhone, phone);
        if (userPhoneMapper.selectOne(userPhoneDOLambdaQueryWrapper) == null) {
            throw new ClientException("当前手机号未注册");
        }

        if (distributedCache.hasKey(phone) != null) {
            throw new ClientException("验证码已发送，请勿重复请求");
        }
        String code = ValidateCodeUtils.generateValidateCode(4).toString();
        log.info("code = {}", code);
        distributedCache.put(phone, code, 180);
    }

    @Transactional
    @Override
    public UserLoginRespDTO loginByCode(UserLoginReqDTO requestParam) {
        String phone = requestParam.getUsernameOrPhone();
        String actualCode = distributedCache.get(phone, String.class);
        String code = requestParam.getCode();
        if (!code.equals(actualCode)) {
            throw new ClientException("验证码错误");
        }
        LambdaQueryWrapper<UserPhoneDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserPhoneDO::getPhone, phone);
        UserPhoneDO userPhoneDO = userPhoneMapper.selectOne(queryWrapper);
        // 如果当前手机号尚未创建用户，则创建
        if (userPhoneDO == null) {
            UserRegisterReqDTO userRegisterReqDTO = UserRegisterReqDTO.builder().phone(phone)
                    .password(RandomUtil.randomString(16))
                    .username(USER_REGISTER_DEFAULT_USERNAME_PREFIX + RandomUtil.randomString(10))
                    .build();
            register(userRegisterReqDTO);
        }
        // 当前用户存在则查询User表
        LambdaQueryWrapper<UserDO> userDOQueryWrapper = Wrappers.lambdaQuery(UserDO.class)
                .eq(UserDO::getUsername, phone);
        UserDO actualUser = userMapper.selectOne(userDOQueryWrapper);
        UserInfoDTO userInfo = UserInfoDTO.builder()
                .userId(String.valueOf(actualUser.getId()))
                .username(actualUser.getUsername())
                .build();
        String accessToken = JWTUtil.generateAccessToken(userInfo);
        UserLoginRespDTO userLoginRespDTO = UserLoginRespDTO.builder()
                .id(String.valueOf(actualUser.getId()))
                .username(actualUser.getUsername())
                .avatar(actualUser.getAvatar())
                .accessToken(accessToken)
                .build();
        distributedCache.put(accessToken, JSON.toJSONString(userLoginRespDTO), 3, TimeUnit.DAYS);
        return userLoginRespDTO;
    }

    @Override
    public UserLoginRespDTO checkLogin(String accessToken) {
        return distributedCache.get(accessToken, UserLoginRespDTO.class);
    }

    @Override
    public void logout(String accessToken) {
        if (StrUtil.isNotBlank(accessToken)) {
            distributedCache.delete(accessToken);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public UserRegisterRespDTO register(UserRegisterReqDTO requestParam) {
        abstractChainContext.handler(USER_REGISTER_FILTER.name(), requestParam);
        RLock lock = redissonClient.getLock(LOCK_USER_REGISTER + requestParam.getPhone());
        boolean tryLock = lock.tryLock();
        if (!tryLock) {
            throw new ClientException(PHONE_REGISTERED);
        }
        UserDO userDO = BeanUtil.convert(requestParam, UserDO.class);
        userDO.setAvatar(USER_REGISTER_DEFAULT_AVATAR_URL);
        try {
            // 1.尝试插入User
            try {
                int inserted = userMapper.insert(userDO);
                if (inserted < 1) {
                    throw new ServiceException(USER_REGISTER_FAIL);
                }
            } catch (DuplicateKeyException dke) {
                log.error("手机号 [{}] 重复注册", requestParam.getPhone());
                throw new ClientException(PHONE_REGISTERED);
            }
            // 2.尝试插入UserPhone
            UserPhoneDO userPhoneDO = UserPhoneDO.builder()
                    .username(requestParam.getUsername())
                    .phone(requestParam.getPhone())
                    .build();
            try {
                userPhoneMapper.insert(userPhoneDO);
            } catch (DuplicateKeyException dke) {
                log.error("用户 [{}] 注册手机号 [{}] 重复", requestParam.getUsername(), requestParam.getPhone());
                throw new ServiceException(PHONE_REGISTERED);
            }
        } finally {
            lock.unlock();
        }
        return BeanUtil.convert(userDO, UserRegisterRespDTO.class);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void deletion(UserDeletionReqDTO requestParam) {
        String phone = UserContext.getPhone();
        if (!UserContext.getPhone().equals(phone)) {
            throw new ClientException("注销账户和登录账户不一致");
        }
        RLock lock = redissonClient.getLock(USER_DELETION + requestParam.getPhone());
        lock.lock();
        try {
            LambdaQueryWrapper<UserDO> queryWrapper = Wrappers.lambdaQuery(UserDO.class)
                    .eq(UserDO::getPhone, requestParam.getPhone());
            UserDO userDO = userMapper.selectOne(queryWrapper);
            userMapper.deletionUser(userDO);

            UserPhoneDO userPhoneDO = UserPhoneDO.builder()
                    .phone(requestParam.getPhone())
                    .username(userDO.getUsername())
                    .build();
            userPhoneMapper.deletionUser(userPhoneDO);

            distributedCache.delete(UserContext.getToken());
        } finally {
            lock.unlock();
        }
    }
}
