package io.mtc.server.zuul.filter;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import io.mtc.server.zuul.util.TokenCheckUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.PatternMatchUtils;

import javax.servlet.http.HttpServletRequest;

/**
 * 所有不能暴露到外网的接口，都在这里拦截
 *
 * @author Chinhin
 * 2018/6/25
 */
@Slf4j
@Component
public class DisableUrlFilter extends ZuulFilter {

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
        return (
            PatternMatchUtils.simpleMatch("/bitcoin/tx/**", request.getRequestURI())
        );
    }

    @Override
    public Object run() {
        RequestContext currentContext = RequestContext.getCurrentContext();
        TokenCheckUtil.errorHandler(currentContext, "disable url", HttpStatus.NOT_ACCEPTABLE);
        return null;
    }

}
