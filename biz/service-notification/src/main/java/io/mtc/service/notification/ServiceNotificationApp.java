package io.mtc.service.notification;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 启动类
 *
 * @author Chinhin
 * 2018/7/9
 */
@SpringBootApplication(scanBasePackages="io.mtc")
@EnableFeignClients
public class ServiceNotificationApp {

    public static void main(String[] args) {
        SpringApplication.run(ServiceNotificationApp.class, args);
    }

}
