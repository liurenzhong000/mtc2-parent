package io.mtc.facade.bitcoin.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@EnableSwagger2
@Configuration
public class Swagger2 {

    @Value("${swagger.show}")
    private boolean swaggerShow;

    @Bean
    public Docket createRestApi() {
        return new Docket(DocumentationType.SWAGGER_2)
                .enable(swaggerShow)
                .apiInfo(apiInfo())
                .select()
                .apis(RequestHandlerSelectors.basePackage("io.mtc.facade.bitcoin.controller"))
                .build();
    }

    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title("MTC2 API")
                .description("所有请求前面都要加上网关地址 https://app.mtc.io/bitcoin/")
                .termsOfServiceUrl("https://app.mtc.io/backend/")
                .version("1.0")
                .build();
    }

}
