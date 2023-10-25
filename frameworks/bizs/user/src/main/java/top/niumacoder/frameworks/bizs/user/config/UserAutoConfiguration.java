package top.niumacoder.frameworks.bizs.user.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import top.niumacoder.frameworks.bizs.user.core.UserTransmitFilter;

import static top.niumacoder.frameworks.bases.constant.FilterOrderConstant.USER_TRANSMIT_FILTER_ORDER;

/**
 * 用户配置自动装配
 */
@ConditionalOnWebApplication
public class UserAutoConfiguration {

    /**
     * 用户信息传递过滤器
     */
    @Bean
    public FilterRegistrationBean<UserTransmitFilter> globalUserTransmitFilter() {
        FilterRegistrationBean<UserTransmitFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new UserTransmitFilter());
        registration.addUrlPatterns("/*");
        registration.setOrder(USER_TRANSMIT_FILTER_ORDER);
        return registration;
    }
}
