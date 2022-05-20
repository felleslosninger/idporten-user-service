package no.idporten.userservice.config;

import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@ConfigurationProperties(ignoreInvalidFields = true, prefix = "idporten-user-service.whitelist-config.security")
@Configuration
@Getter
@Component
public class WebSecurityProperties {

    private final List<String> getAllowed = new ArrayList<>();
}
