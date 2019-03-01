package io.mtc.server.zuul.filter;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import io.mtc.common.util.StringUtil;
import io.mtc.server.zuul.util.RequestCheckUtil;
import io.mtc.server.zuul.util.TokenCheckUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.PatternMatchUtils;

import javax.servlet.http.HttpServletRequest;

/**
 * 过滤器
 *
 * @author Chinhin
 * 2018/6/25
 */
@Slf4j
@Component
public class ApiValidateFilter extends ZuulFilter {

    @Value("${enableFilter.api}")
    private boolean enableFilter;

    @Override
    public String filterType() {
        return "pre";
    }

    @Override
    public int filterOrder() {
        return 0;
    }

    @Override
    public boolean shouldFilter() {
        HttpServletRequest request = RequestContext.getCurrentContext().getRequest();
        return enableFilter && (
                        PatternMatchUtils.simpleMatch("/api/*", request.getRequestURI())
                        || PatternMatchUtils.simpleMatch("/user/*", request.getRequestURI())
                        || PatternMatchUtils.simpleMatch("/market/*", request.getRequestURI())
                        || PatternMatchUtils.simpleMatch("/bitcoinx/*", request.getRequestURI())
        );
    }

    @Override
    public Object run() {
        RequestContext currentContext = RequestContext.getCurrentContext();
        HttpServletRequest request = currentContext.getRequest();
        String requestURI = request.getRequestURI();
        if (PatternMatchUtils.simpleMatch(
                new String[]{
                        "/*/v2/api-docs*"
                        ,"/*/swagger*"
                        ,"/*/webjars*"
                        ,"/user/user/register*" // 为h5的注册及验证码开通
                        ,"/user/user/sendCode*"
                        ,"/user/eos/eosApi/**"
                },
                requestURI)) {
            return null;
        }

        String requestTime = request.getHeader("requestTime");
        String signature = request.getHeader("signature");
        log.info("uri:{}, requestTime:{}, signature:{}", requestURI, requestTime, signature);
        if (StringUtil.isBlank(requestTime) || StringUtil.isBlank(signature)) {
            TokenCheckUtil.errorHandler(currentContext, "signature error", HttpStatus.UNAUTHORIZED);
            return null;
        }
        Long timeLong;
        try {
            timeLong = Long.valueOf(requestTime);
        } catch (NumberFormatException e) {
            TokenCheckUtil.errorHandler(currentContext, "signature error", HttpStatus.UNAUTHORIZED);
            return null;
        }

        boolean checkResult = RequestCheckUtil.validateRequest(timeLong, requestURI, signature);
        // 未通过check
        if (!checkResult) {
            TokenCheckUtil.errorHandler(currentContext, "signature error", HttpStatus.UNAUTHORIZED);
        }
        return null;
    }

}
