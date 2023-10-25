package top.niumacoder.frameworks.bizs.user.toolkit;

import com.alibaba.fastjson2.JSON;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.extern.slf4j.Slf4j;
import top.niumacoder.frameworks.bizs.user.core.UserInfoDTO;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.HashMap;

import static top.niumacoder.frameworks.bases.constant.UserConstant.*;

/**
 * JWT工具类
 */
@Slf4j
public final class JWTUtil {
    private static final long EXPIRATION = 86400L;
    public static final String TOKEN_PREFIX = "qiniu ";
    public static final String ISS = "niuma";
    public static final String SECRET = "NiuMaKey30181025841234815";

    /**
     * 生成用户Token
     *
     * @param userInfo - 用户信息
     * @return 用户访问Token
     */
    public static String generateAccessToken(UserInfoDTO userInfo) {
        HashMap<String, Object> userInfoMap = new HashMap<>();
        userInfoMap.put(USER_ID_KEY, userInfo.getUserId());
        userInfoMap.put(USER_NAME_KEY, userInfo.getUsername());
        userInfoMap.put(USER_TOKEN_KEY, userInfo.getToken());
        String jwtToken = Jwts.builder()
                .signWith(SignatureAlgorithm.HS512, SECRET)
                .setIssuedAt(new Date())
                .setIssuer(ISS)
                .setSubject(JSON.toJSONString(userInfoMap))
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION * 1000))
                .compact();
        return TOKEN_PREFIX + jwtToken;
    }

    public static UserInfoDTO parseJwtToken(String jwtToken) {
        if (StringUtils.hasText(jwtToken)) {
            String actualToken = jwtToken.replace(TOKEN_PREFIX, "");
            try {
                Claims claims = Jwts.parser().setSigningKey(SECRET).parseClaimsJwt(actualToken).getBody();
                Date expiration = claims.getExpiration();
                if (expiration.after(new Date())) {
                    String subject = claims.getSubject();
                    return JSON.parseObject(subject, UserInfoDTO.class);
                }
            } catch (ExpiredJwtException ignored) {
            } catch (Exception ex) {
                log.error("JWT Token解析错误", ex);
            }
        }
        return null;
    }

}
