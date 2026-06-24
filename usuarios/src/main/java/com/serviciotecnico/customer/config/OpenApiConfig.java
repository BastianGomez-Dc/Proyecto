package com.serviciotecnico.customer.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customerServiceApi() {
        return new OpenAPI().info(new Info()
                .title("Customer Service API")
                .description("Manages workshop customers and their login")
                .version("v1"));
    }
}
