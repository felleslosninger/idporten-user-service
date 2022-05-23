package no.idporten.userservice.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI(@Value("${project.version}") String appVersion) {
        return new OpenAPI()
                .info(new Info()
                        .title("ID-porten user service")
                        .version(appVersion)
                        .description("ID-porten user service"))
                .components(new Components().addSecuritySchemes("basic_auth", securitySchemeBasicAuth()).addSecuritySchemes("access_token", securitySchemeBearer()));
    }

    private io.swagger.v3.oas.models.security.SecurityScheme securitySchemeBearer() {
        return new io.swagger.v3.oas.models.security.SecurityScheme().type(io.swagger.v3.oas.models.security.SecurityScheme.Type.HTTP).scheme("bearer").bearerFormat("access_token");
    }
    private io.swagger.v3.oas.models.security.SecurityScheme securitySchemeBasicAuth() {
        return new io.swagger.v3.oas.models.security.SecurityScheme().type(io.swagger.v3.oas.models.security.SecurityScheme.Type.HTTP).scheme("basic");
    }

}
