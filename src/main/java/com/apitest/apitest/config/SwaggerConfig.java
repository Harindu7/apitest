package com.apitest.apitest.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI myOpenAPI() {
        Server devServer = new Server();
        devServer.setUrl("http://localhost:8080");
        devServer.setDescription("Local Development Server Third Party API Test");

        Info info = new Info()
                .title("Test Third Party API")
                .version("1.0")
                .description("This API provides endpoints to test APIs.");

        return new OpenAPI()
                .info(info)
                .servers(List.of(devServer));
    }
}
