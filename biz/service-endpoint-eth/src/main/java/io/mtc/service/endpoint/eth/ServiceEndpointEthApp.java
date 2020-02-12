package io.mtc.service.endpoint.eth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 启动类
 *
 * @author Chinhin
 * 2018/6/21
 */
@EnableScheduling
@SpringBootApplication(scanBasePackages = "io.mtc", exclude = {DataSourceAutoConfiguration.class})
public class ServiceEndpointEthApp {

    public static void main(String[] args) {
        SpringApplication.run(ServiceEndpointEthApp.class, args);
    }

}
