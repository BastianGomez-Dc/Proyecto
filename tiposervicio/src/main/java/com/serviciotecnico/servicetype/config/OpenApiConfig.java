package com.serviciotecnico.servicetype.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI serviceTypeServiceApi() {
        return new OpenAPI().info(new Info()
                .title("Service Type Service API")
                .description("Catalog of available technical service types")
                .version("v1"));
    }
}
