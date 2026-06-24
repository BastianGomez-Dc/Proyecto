package com.serviciotecnico.computer.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI computerServiceApi() {
        return new OpenAPI().info(new Info()
                .title("Computer Service API")
                .description("Manages customer computers and their hardware components")
                .version("v1"));
    }
}
