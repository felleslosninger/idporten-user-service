package no.idporten.userservice.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.boot.info.BuildProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Optional;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI(Optional<BuildProperties> buildProperties) {
        return new OpenAPI()
                .info(new Info()
                        .title("ID-porten user service")
                        .version(buildProperties.isPresent() ?
                                buildProperties.get().getVersion() :
                                "unknown")
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
