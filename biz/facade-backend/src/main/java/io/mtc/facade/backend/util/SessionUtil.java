package io.mtc.facade.backend.util;

import io.mtc.facade.backend.entity.AdminPermission;
import io.mtc.facade.backend.entity.AdminUser;
import org.springframework.security.core.context.SecurityContextImpl;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * spring security session工具类
 *
 * @author Chinhin
 * 2018/6/11
 */
public class SessionUtil {

    public static boolean isLogin(HttpServletRequest request) {
        Object springContext = request.getSession().getAttribute("SPRING_SECURITY_CONTEXT");
        return springContext != null;
    }

    public static String sessionId(HttpServletRequest request) {
        return request.getSession().getId();
    }

    /**
     * 获取登录用户的ID
     * @param request 请求
     * @return id
     */
    public static Long adminId(HttpServletRequest request) {
        Object springContext = request.getSession().getAttribute("SPRING_SECURITY_CONTEXT");
        if (springContext != null) {
            SecurityContextImpl context = (SecurityContextImpl) springContext;
            AdminUser adminUser = (AdminUser) context.getAuthentication().getPrincipal();
            return adminUser.getId();
        }
        return 0L;
    }

    /**
     * 获取登录用户的权限
     */
    public static Map<String, Boolean> auths(HttpServletRequest request) {
        Object springContext = request.getSession().getAttribute("SPRING_SECURITY_CONTEXT");
        if (springContext != null) {
            SecurityContextImpl context = (SecurityContextImpl) springContext;
            AdminUser adminUser = (AdminUser) context.getAuthentication().getPrincipal();
            Set<AdminPermission> permissions = adminUser.getRole().getPermissions();
            Map<String, Boolean> result = new HashMap<>();
            permissions.forEach(it -> {
                result.put(it.getPermission(), true);
            });
            return result;
        }
        return null;
    }
}
