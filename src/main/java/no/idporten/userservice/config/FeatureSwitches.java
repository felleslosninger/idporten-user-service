package no.idporten.userservice.config;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import no.idporten.validators.identifier.PersonIdentifierValidator;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

@Configuration
@Data
@Slf4j
@Validated
@ConfigurationProperties(prefix = "idporten-user-service.features")
public class FeatureSwitches implements InitializingBean {

    private boolean allowSyntheticPid = false;
    private boolean allowRealPid = true;

    @Override
    public void afterPropertiesSet() {
        PersonIdentifierValidator.setSyntheticPersonIdentifiersAllowed(isAllowSyntheticPid());
        PersonIdentifierValidator.setRealPersonIdentifiersAllowed(isAllowRealPid());
        log.info("Will {}handle synthetic person identifiers.", isAllowSyntheticPid() ? "" : "not ");
        log.info("Will {}handle real person identifiers.", isAllowRealPid() ? "" : "not ");
    }

}
