package top.niumacoder.filter;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import top.niumacoder.config.Config;
import top.niumacoder.frameworks.bases.constant.UserConstant;
import top.niumacoder.toolkit.JWTUtil;
import top.niumacoder.toolkit.UserInfoDTO;

import java.util.Objects;

@Component
public class TokenValidateGatewayFilterFactory extends AbstractGatewayFilterFactory<Config> {

    /**
     * 注销用户时需要传递 Token
     */
    public static final String DELETION_PATH = "/api/user-service/deletion";

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            String token = request.getHeaders().getFirst("Authorization");
            if (StringUtils.hasText(token)) {
                UserInfoDTO userInfo = JWTUtil.parseJwtToken(token);
                if (validateToken(userInfo)) {
                    ServerHttpRequest.Builder builder = exchange.getRequest().mutate().headers(httpHeaders -> {
                        httpHeaders.set(UserConstant.USER_ID_KEY, userInfo.getUserId());
                        httpHeaders.set(UserConstant.USER_NAME_KEY, userInfo.getUsername());
                        httpHeaders.set(UserConstant.USER_PHONE_KEY, userInfo.getPhone());
                        if (Objects.equals(request.getPath().toString(), DELETION_PATH)) {
                            httpHeaders.set(UserConstant.USER_TOKEN_KEY, token);
                        }
                    });
                    return chain.filter(exchange.mutate().request(builder.build()).build());
                }
            }
            return chain.filter(exchange);
        };
    }

    private boolean validateToken(UserInfoDTO userInfo) {
        return userInfo != null;
    }
}
