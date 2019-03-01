package io.mtc.server.register;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 启动类
 *
 * @author Chinhin
 * 2018/6/9
 */
@EnableScheduling
@EnableEurekaServer
@SpringBootApplication(scanBasePackages="io.mtc")
public class ServerRegisterApp {

    public static void main(String[] args) {
        SpringApplication.run(ServerRegisterApp.class, args);
    }

}
