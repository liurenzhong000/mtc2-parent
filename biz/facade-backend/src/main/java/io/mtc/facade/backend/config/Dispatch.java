package io.mtc.facade.backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 跳转配置
 *
 * @author Chinhin
 * 2018/7/3
 */
@Configuration
public class Dispatch implements WebMvcConfigurer {

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/customPage/*").setViewName("/customPage.html");
        registry.addViewController("/share/*").setViewName("/share.html");
        // 手机端h5页面
        registry.addViewController("/phone/register/**").setViewName("/phone/register.html");
    }

}
