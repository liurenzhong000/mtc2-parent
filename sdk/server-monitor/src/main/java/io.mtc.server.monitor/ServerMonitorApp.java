package io.mtc.server.monitor;

import de.codecentric.boot.admin.server.config.EnableAdminServer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * 启动类
 *
 * @author Chinhin
 * 2018/7/31
 */
@EnableAdminServer
@EnableDiscoveryClient
@SpringBootApplication
public class ServerMonitorApp {

    public static void main(String[] args) {
        SpringApplication.run(ServerMonitorApp.class, args);
    }

}
