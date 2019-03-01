package io.mtc.facade.user;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 启动类
 *
 * @author Chinhin
 * 2018/7/23
 */
@EnableFeignClients
@SpringBootApplication(scanBasePackages = "io.mtc")
public class FacadeUserApp {

    public static void main(String[] args) {
        SpringApplication.run(FacadeUserApp.class, args);
    }

}
