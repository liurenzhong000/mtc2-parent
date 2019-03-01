package io.mtc.server.zuul;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.netflix.zuul.EnableZuulProxy;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import springfox.documentation.swagger.web.SwaggerResource;
import springfox.documentation.swagger.web.SwaggerResourcesProvider;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.ArrayList;
import java.util.List;

/**
 * 启动类
 *
 * @author Chinhin
 * 2018/6/10
 */
@EnableZuulProxy
@EnableSwagger2
@EnableDiscoveryClient
@SpringBootApplication(scanBasePackages = "io.mtc")
public class ServerZuulApp {
    public static void main(String[] args) {
        SpringApplication.run(ServerZuulApp.class, args);
    }

    @Component
    @Primary
    class DocumentationConfig implements SwaggerResourcesProvider {
        @Override
        public List<SwaggerResource> get() {
            List<SwaggerResource> resources = new ArrayList<>();
            resources.add(swaggerResource("api ⊿ 以太坊交易", "/api/v2/api-docs", "1.0"));
            resources.add(swaggerResource("user ⊿ 托管账户相关", "/user/v2/api-docs", "2.0"));
            resources.add(swaggerResource("market ⊿ 行情相关", "/market/v2/api-docs", "2.0"));
            resources.add(swaggerResource("bitcoin ⊿ 比特系", "/bitcoin/v2/api-docs", "1.0"));
            return resources;
        }

        private SwaggerResource swaggerResource(String name, String location, String version) {
            SwaggerResource swaggerResource = new SwaggerResource();
            swaggerResource.setName(name);
            swaggerResource.setLocation(location);
            swaggerResource.setSwaggerVersion(version);
            return swaggerResource;
        }
    }
}
