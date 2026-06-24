package com.serviciotecnico.maintenance.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI maintenanceServiceApi() {
        return new OpenAPI().info(new Info()
                .title("Maintenance Service API")
                .description("Manages maintenance work orders (tickets) for customer computers")
                .version("v1"));
    }
}
