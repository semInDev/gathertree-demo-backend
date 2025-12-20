package com.gathertree.demo.global.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/*
 Swagger 접속 URL
 - Local : http://localhost:8080/swagger-ui/index.html
 - Prod  : https://api.beour.store/swagger-ui/index.html (인프라 구성 후)
 */
@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("🎄 GatherTree API")
                        .description("GatherTree 크리스마스 이벤트 백엔드 API 명세")
                        .version("v1")
                )
                .servers(List.of(
                        // ✅ 지금은 로컬만
                        new Server()
                                .url("http://localhost:8080")
                                .description("Local server")

                        // 🚫 인프라 구성 전이므로 제거
                        // new Server()
                        //        .url("https://api.beour.store")
                        //        .description("Production server")
                ));
    }
}
