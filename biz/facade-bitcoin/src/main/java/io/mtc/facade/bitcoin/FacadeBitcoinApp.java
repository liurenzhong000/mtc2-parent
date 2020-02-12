package io.mtc.facade.bitcoin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 启动类
 *
 * @author Chinhin
 * 2018/12/10
 */
@SpringBootApplication(scanBasePackages="io.mtc")
@EnableFeignClients
public class FacadeBitcoinApp {
    public static void main(String[] args) {
        SpringApplication.run(FacadeBitcoinApp.class, args);
    }
}
