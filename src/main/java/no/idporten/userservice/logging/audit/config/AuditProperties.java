package no.idporten.userservice.logging.audit.config;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotNull;

@Configuration
@Data
@Slf4j
@Validated
@ConfigurationProperties(prefix = "idporten-user-service.audit")
public class AuditProperties {

    @NotNull
    private String applicationName;

    private String auditLogDir;

}
