package top.niumacoder.userservice.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import top.niumacoder.userservice.dao.entity.UserPhoneDO;

public interface UserPhoneMapper extends BaseMapper<UserPhoneDO> {
    /**
     * 注销用户
     *
     * @param userPhoneDO 注销用户入参
     */
    void deletionUser(UserPhoneDO userPhoneDO);
}
