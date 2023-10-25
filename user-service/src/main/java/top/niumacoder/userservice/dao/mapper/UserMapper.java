package top.niumacoder.userservice.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import top.niumacoder.userservice.dao.entity.UserDO;

public interface UserMapper extends BaseMapper<UserDO> {
    void deletionUser(UserDO user);
}
