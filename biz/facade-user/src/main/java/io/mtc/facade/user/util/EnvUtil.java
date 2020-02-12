package io.mtc.facade.user.util;

import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @Auther: hyp
 * @Date: 2019/3/12 16:04
 * @Description:
 */
@Component
public class EnvUtil {

    @Resource
    private ApplicationContext applicationContext;

    public String getEnv(){
        return applicationContext.getEnvironment().getActiveProfiles()[0];
    }

    public boolean isProd(){
        return getEnv().equalsIgnoreCase("prod");
    }

}
