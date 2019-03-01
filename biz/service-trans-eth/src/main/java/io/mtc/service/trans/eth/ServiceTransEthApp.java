package io.mtc.service.trans.eth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 启动类
 *
 * @author Chinhin
 * 2018/6/25
 */
@SpringBootApplication(scanBasePackages="io.mtc", exclude = {DataSourceAutoConfiguration.class})
@EnableFeignClients
public class ServiceTransEthApp {

    public static void main(String[] args) {
        SpringApplication.run(ServiceTransEthApp.class, args);
    }

}
