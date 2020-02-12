package io.mtc.server.zuul.util;

import com.netflix.zuul.context.RequestContext;
import io.mtc.common.redis.constants.RedisKeys;
import io.mtc.common.redis.util.RedisUtil;
import io.mtc.common.util.StringUtil;
import org.springframework.http.HttpStatus;

/**
 * token检查的工具
 *
 * @author Chinhin
 * 2018/12/11
 */
public class TokenCheckUtil {

    /**
     * 检查token是否正常
     * @param uid 用户id
     * @param token 请求传的token
     * @param redisUtil 缓存
     * @return 结果true表示正常，false表示token异常
     */
    public static boolean isTokenOK(String uid, String token, RedisUtil redisUtil) {
        if (StringUtil.isBlank(uid) || StringUtil.isBlank(token)) {
            return false;
        } else {
            String dbToken = redisUtil.get(RedisKeys.USER_TOKEN(uid), String.class);
            return !StringUtil.isBlank(dbToken) && dbToken.equals(token);
        }
    }

    /**
     * token检查失败的错误
     */
    public static void errorHandler(RequestContext currentContext, String body, HttpStatus httpStatus) {
        currentContext.setSendZuulResponse(false);
        currentContext.setResponseBody(body);
        currentContext.setResponseStatusCode(httpStatus.value());
    }

}
