package top.niumacoder.userservice.dao.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import top.niumacoder.frameworks.database.base.BaseDO;

/**
 * 用户信息实体
 */
@Data
@TableName("user")
public class UserDO extends BaseDO {

    /**
     * id
     */
    private Long id;

    /**
     * 用户名
     */
    private String username;

    /**
     * 密码
     */
    private String password;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 头像
     */
    private String avatar;
    
    /**
     * 注销时间戳
     */
    private Long deletionTime;
}
