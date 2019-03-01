package io.mtc.facade.backend.config.security;

import io.mtc.common.util.ResultUtil;
import io.mtc.facade.backend.entity.AdminUser;
import io.mtc.facade.backend.model.SessionUser;
import io.mtc.facade.backend.repository.AdminUserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.security.web.authentication.logout.SimpleUrlLogoutSuccessHandler;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * spring security 配置类
 *
 * @author Chinhin
 * 2018/6/8
 */
@Slf4j
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Resource
    private SecurityPwdEncode securityPwdEncode;

    @Resource
    private AdminUserRepository adminUserRepository;

    private static final String[] permitAllUrl = new String[] {
            "/actuator/**",
            "/share/**",
            "/phone/**",
            "/login.html",
            "/doLogin",
            "/isLogin",
            "/page",
            "/customPage/*",
            "/custom/detail/**",
            "/appVersion/info",
            "/css/**",
            "/js/**",
            "/plugins/**",
            "/fonts/**",
            "/font-awesome/**",
            "/img/**"
    };

    @Value("${zuul-backend-url}")
    private String zuulBackendUrl;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        String loginUrl = zuulBackendUrl + "/login.html";
//        String loginUrl = "/login";

        //  定义当需要用户登录时候，转到的登录页面
        http
                .exceptionHandling().authenticationEntryPoint(new UnauthorizedEntryPoint(loginUrl))
                .and()
                .csrf().disable()
                .authorizeRequests()
                // 设置所有人都可以访问登录页面
                .antMatchers(permitAllUrl).permitAll()
                // 任何请求,登录后可以访问
                .anyRequest().authenticated()
                .and()
                .formLogin()
                .loginPage(loginUrl)
                .loginProcessingUrl("/doLogin")
                .usernameParameter("username")
                .passwordParameter("password")
                .successHandler(new SimpleUrlAuthenticationSuccessHandler() {
                    @Override
                    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
                        logger.info("登录成功");

                        response.setContentType("application/json;charset=UTF-8");

                        SessionUser sessionUser = new SessionUser();
                        AdminUser adminUser = (AdminUser) authentication.getPrincipal();
                        BeanUtils.copyProperties(adminUser, sessionUser);
                        sessionUser.setRoleName(adminUser.getRole().getRolename());

                        Map<String, Boolean> auths = new HashMap<>();
                        adminUser.getRole().getPermissions().forEach(it -> auths.put(it.getPermission(), true));
                        sessionUser.setAuths(auths);
                        response.getWriter().write(ResultUtil.success(sessionUser));
                    }
                })
                .failureHandler(new SimpleUrlAuthenticationFailureHandler() {
                    @Override
                    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException {
                        logger.info("登录失败");

                        response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
                        response.setContentType("application/json;charset=UTF-8");
                        response.getWriter().write(ResultUtil.error(exception.getMessage()));

                    }
                })
                .permitAll()
                .and()
                .logout()
                .logoutUrl("/logout")
                .logoutSuccessHandler(new SimpleUrlLogoutSuccessHandler(){
                    @Override
                    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
                        response.setContentType("application/json;charset=UTF-8");
                        response.getWriter().write(ResultUtil.success("退出成功"));
                    }
                })
                .deleteCookies("JSESSIONID").invalidateHttpSession(true)
                .permitAll();
//                .and().sessionManagement().invalidSessionUrl(loginUrl).maximumSessions(1).maxSessionsPreventsLogin(false)
//                .expiredUrl(loginUrl);

        http.headers().frameOptions().sameOrigin();
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(new UserDetailsService() {

            @Transactional
            @Override
            public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
                log.info("登录用户: {}", username);
                AdminUser user = adminUserRepository.findByUsername(username);
                if (user == null) {
                    throw new UsernameNotFoundException("用户名不存在");
                }
                // 将所有权限的key用comma连接起来
                StringBuffer permissionBuf = new StringBuffer();
                user.getRole().getPermissions().forEach(it -> permissionBuf.append(it.getPermission()).append(","));
                permissionBuf.deleteCharAt(permissionBuf.length() - 1);
                user.setAuths(AuthorityUtils.commaSeparatedStringToAuthorityList(permissionBuf.toString()));
                return user;

        }}).passwordEncoder(securityPwdEncode);
    }

}
