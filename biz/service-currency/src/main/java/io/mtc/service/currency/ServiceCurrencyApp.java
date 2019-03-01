package io.mtc.service.currency;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 启动类
 *
 * @author Chinhin
 * 2018/6/15
 */
@EnableFeignClients
@SpringBootApplication(scanBasePackages="io.mtc")
public class ServiceCurrencyApp {

    public static void main(String[] args) {
        SpringApplication.run(ServiceCurrencyApp.class, args);
    }

}
