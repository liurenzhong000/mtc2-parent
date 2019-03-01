package io.mtc.facade.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 启动类
 *
 * @author Chinhin
 * 2018/6/17
 */
@SpringBootApplication(scanBasePackages="io.mtc")
@EnableFeignClients
public class FacadeApiMarketApp {

    public static void main(String[] args) {
        SpringApplication.run(FacadeApiMarketApp.class, args);
    }

}
