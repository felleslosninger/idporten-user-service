package no.idporten.userservice.logging.audit.config;

import lombok.extern.slf4j.Slf4j;
import no.idporten.logging.audit.AuditConfig;
import no.idporten.logging.audit.AuditLogger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

@Configuration
@Slf4j
public class AuditConfiguration {

    @Bean
    public AuditLogger auditLogger(AuditProperties auditProperties) {
        log.info("Application will audit log to {}.",
                StringUtils.hasText(auditProperties.getAuditLogDir())
                        ?
                        auditProperties.getAuditLogDir()
                        :
                        "console");
        return new AuditLogger(AuditConfig.builder()
                .applicationName(auditProperties.getApplicationName())
                .logfileDir(auditProperties.getAuditLogDir())
                .build());
    }

}
