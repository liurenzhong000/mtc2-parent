package io.mtc.facade.backend.config.security;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 登录设置
 *
 * @author Chinhin
 * 2018/6/11
 */
public class UnauthorizedEntryPoint implements AuthenticationEntryPoint {

    private String loginUrl;

    public UnauthorizedEntryPoint(String loginUrl) {
        this.loginUrl = loginUrl;
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
        if(isAjaxRequest(request)){
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, authException.getMessage());
        }else{
            response.sendRedirect(loginUrl);
        }
    }

    public static boolean isAjaxRequest(HttpServletRequest request) {
        String ajaxFlag = request.getHeader("X-Requested-With");
        return "XMLHttpRequest".equals(ajaxFlag);
    }
}
