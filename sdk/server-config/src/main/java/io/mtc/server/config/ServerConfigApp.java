package io.mtc.server.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.config.server.EnableConfigServer;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

/**
 * 启动类
 *
 * @author Chinhin
 * 2018/6/9
 */
@SpringBootApplication
@EnableConfigServer
@EnableEurekaClient
public class ServerConfigApp {

    public static void main(String[] args) {
        SpringApplication.run(ServerConfigApp.class, args);
    }

}
