package com.studio.api.global.config.swagger;

import com.studio.core.global.exception.ErrorCode;
import com.studio.core.global.response.ErrorResponse;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.examples.Example;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import jakarta.servlet.ServletContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.*;

import static com.studio.core.global.exception.ErrorCode.PARAMETER_VALIDATION_ERROR;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI(ServletContext servletContext) {
        Server localServer = new Server();
        Server devServer = new Server();
        Server prodServer = new Server();
        localServer.setUrl("http://localhost:8080");
        localServer.setDescription("local Server");
        devServer.setUrl("https://api.oneschedule.site");
        devServer.setDescription("dev Server");
        return new OpenAPI()
                .info(_setApiInfo())
                .components(_setComponents())
                .addSecurityItem(_setSecurityItems())
                .servers(List.of(localServer, devServer));
    }


    private Info _setApiInfo() {
        return new Info()
                .version("1.0.0")
                .title("schedule API 명세서")
                .description("schedule 서비스 API 명세서입니다.");
    }

    private Components _setComponents() {
        return new Components()
                .addSecuritySchemes("access-token", _getJwtSecurityScheme());
    }

    private SecurityScheme _getJwtSecurityScheme() {
        return new SecurityScheme()
                .type(SecurityScheme.Type.APIKEY)
                .in(SecurityScheme.In.COOKIE)
                .name("access-token");
    }

    private SecurityRequirement _setSecurityItems() {
        return new SecurityRequirement()
                .addList("access-token");
    }

    private Example getSwaggerExample(ErrorCode errorCode) {
        Map<String, String> result = new LinkedHashMap<>();

        if (errorCode.getErrorCode() == PARAMETER_VALIDATION_ERROR.getErrorCode()) {
            result.put("key", "검증 대상 파라미터");
            result.put("value", "받은 파라미터 값");
            result.put("reason", "검증 에러 원인 메세지");
        }

        ErrorResponse errorResponse = new ErrorResponse(errorCode.getErrorCode(), errorCode.getMessage(), result);
        Example example = new Example();
        example.description(errorCode.getMessage());
        example.setValue(errorResponse);
        return example;
    }

}
