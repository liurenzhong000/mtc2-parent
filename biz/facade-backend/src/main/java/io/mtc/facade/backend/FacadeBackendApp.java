package io.mtc.facade.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

/**
 * 启动类
 *
 * @author Chinhin
 * 2018/6/10
 */
@SpringBootApplication(scanBasePackages="io.mtc", exclude = {
        org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class
})
@EnableRedisHttpSession
@EnableFeignClients
public class FacadeBackendApp {

    public static void main(String[] args) {
        SpringApplication.run(FacadeBackendApp.class, args);
    }

}
