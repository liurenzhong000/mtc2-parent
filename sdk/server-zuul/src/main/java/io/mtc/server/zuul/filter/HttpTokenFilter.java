package io.mtc.server.zuul.filter;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import io.mtc.common.constants.Constants;
import io.mtc.common.redis.util.RedisUtil;
import io.mtc.common.util.AesCBC;
import io.mtc.common.util.CommonUtil;
import io.mtc.common.util.StringUtil;
import io.mtc.server.zuul.util.EncryptUtil;
import io.mtc.server.zuul.util.TokenCheckUtil;
import io.mtc.server.zuul.util.Urls;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.netflix.zuul.filters.support.FilterConstants;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.PatternMatchUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.*;

/**
 * 非https请求用户模块的，对请求进行解密处理，并对token进行验证
 *
 * @author Chinhin
 * 2018/12/11
 */
@Slf4j
@Component
public class HttpTokenFilter extends ZuulFilter {

    @Value("${enableFilter.http:true}")
    private boolean enableFilter;

    @Resource
    private RedisUtil redisUtil;

    @Override
    public String filterType() {
        return FilterConstants.PRE_TYPE;
    }

    @Override
    public int filterOrder() {
        return 0;
    }

    @Override
    public boolean shouldFilter() {
        HttpServletRequest request = RequestContext.getCurrentContext().getRequest();
        String encrypt = request.getHeader("encrypt");
        log.info("请求 {}, 是否加密: {}", request.getRequestURI(), encrypt != null);
        return enableFilter
                && PatternMatchUtils.simpleMatch("/user/*", request.getRequestURI())
                && encrypt != null;
    }

    @Override
    public Object run() {
        RequestContext currentContext = RequestContext.getCurrentContext();
        HttpServletRequest request = currentContext.getRequest();
        String requestURI = request.getRequestURI();

        String headerToken = request.getHeader("token");
        if (StringUtil.isNotEmpty(headerToken)) {
            TokenCheckUtil.errorHandler(currentContext, "dont push token in header", HttpStatus.METHOD_NOT_ALLOWED);
            return null;
        }
        // 没有验证token的，且没有加密的请求直接放行
        if (Urls.isUncheckUri(requestURI) && !Urls.isPwdUri(requestURI)) {
            return null;
        }

        // 获取到的是加密的参数json体
        String encryParamStr = null;
        if (request.getMethod().equals(HttpMethod.GET.toString())) {
            try {
                encryParamStr = URLDecoder.decode(request.getQueryString(), "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        } else {
            encryParamStr = Urls.getBody(request);
        }
        log.info("3333={}",encryParamStr);
        String requestParamStr = Constants.EMPTY;
        if (StringUtil.isNotEmpty(encryParamStr)) {
            String requestTime = request.getHeader("requestTime");
            String signature = request.getHeader("signature");
            try {
                requestParamStr = AesCBC.getInstance().simpleDecrypt(encryParamStr,
                        EncryptUtil.makeKey(Long.valueOf(requestTime), signature));
            } catch (Exception e) {
                TokenCheckUtil.errorHandler(currentContext, "decrypt failure", HttpStatus.NOT_ACCEPTABLE);
                return null;
            }
        }
        // 非token的请求要验证token
        if (!Urls.isPwdUri(requestURI)) {
            if (StringUtil.isEmpty(requestParamStr)) {
                TokenCheckUtil.errorHandler(currentContext, "token expire", HttpStatus.FORBIDDEN);
                return null;
            } else {
                JSONObject requestParam = JSON.parseObject(requestParamStr);
                String uid = request.getHeader("uid");
                String token = requestParam.getString("token");
                if (!TokenCheckUtil.isTokenOK(uid, token, redisUtil)) {
                    TokenCheckUtil.errorHandler(currentContext, "token expire", HttpStatus.FORBIDDEN);
                    return null;
                }
            }
        }

        // 不需要重写参数
        if (StringUtil.isEmpty(requestParamStr)) {
            return null;
        }

        // 下面是为请求重写参数
        request.getParameterMap();
        Map<String, List<String>> requestQueryParams = currentContext.getRequestQueryParams();
        if (requestQueryParams == null) {
            requestQueryParams = new HashMap<>();
        }
        Map<String, Object> requestParamMap = CommonUtil.jsonToMap(requestParamStr);
        Iterator entries = requestParamMap.entrySet().iterator();
        while (entries.hasNext()) {
            Map.Entry entry = (Map.Entry) entries.next();
            String key = (String) entry.getKey();
            String value = String.valueOf(entry.getValue());

            List<String> tempList = new ArrayList<>();
            tempList.add(value);
            requestQueryParams.put(key, tempList);
        }
        currentContext.setRequestQueryParams(requestQueryParams);
        return null;
    }

}
