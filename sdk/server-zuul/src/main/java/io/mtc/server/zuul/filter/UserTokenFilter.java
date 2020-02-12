package io.mtc.server.zuul.filter;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import io.mtc.common.redis.util.RedisUtil;
import io.mtc.server.zuul.util.TokenCheckUtil;
import io.mtc.server.zuul.util.Urls;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.PatternMatchUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * https的请求user模块的，进行token的检查
 *
 * @author Chinhin
 * 2018/6/25
 */
@Slf4j
@Component
public class UserTokenFilter extends ZuulFilter {

    @Value("${enableFilter.user}")
    private boolean enableFilter;

    @Resource
    private RedisUtil redisUtil;

    @Override
    public String filterType() {
        return "pre";
    }

    @Override
    public int filterOrder() {
        return 1;
    }

    @Override
    public boolean shouldFilter() {
        HttpServletRequest request = RequestContext.getCurrentContext().getRequest();
        String encrypt = request.getHeader("encrypt");
        return enableFilter
                && PatternMatchUtils.simpleMatch("/user/*", request.getRequestURI())
                && encrypt == null;
    }

    @Override
    public Object run() {
        RequestContext currentContext = RequestContext.getCurrentContext();
        HttpServletRequest request = currentContext.getRequest();
        String requestURI = request.getRequestURI();

        if (Urls.isUncheckUri(requestURI)) {
            return null;
        }

        String token = request.getHeader("token");
        String uid = request.getHeader("uid");
        if (!TokenCheckUtil.isTokenOK(uid, token, redisUtil)) {
            TokenCheckUtil.errorHandler(currentContext, "token expire", HttpStatus.FORBIDDEN);
        }
        return null;
    }


}
