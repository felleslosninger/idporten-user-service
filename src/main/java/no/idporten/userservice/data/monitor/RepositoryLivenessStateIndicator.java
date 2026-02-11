package no.idporten.userservice.data.monitor;

import lombok.extern.slf4j.Slf4j;
import no.idporten.userservice.data.UserRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.availability.ApplicationAvailability;
import org.springframework.boot.health.application.LivenessStateHealthIndicator;
import org.springframework.boot.health.contributor.Health;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component("repositoryLiveness")
@Slf4j
@ConditionalOnProperty(prefix = "management.health.repositoryLiveness", name = "enabled", matchIfMissing = true)
public class RepositoryLivenessStateIndicator extends LivenessStateHealthIndicator {

    private final UserRepository userRepository;

    public RepositoryLivenessStateIndicator(ApplicationAvailability availability, UserRepository userRepository) {
        super(availability);
        this.userRepository = userRepository;
    }

    @Override
    public Health health(boolean includeDetails) {
        try {
            userRepository.findByUuid(UUID.randomUUID());
            return Health.up().build();
        } catch (Exception e) {
            log.error("Health check to repository failed with exception {}", e.getMessage(), e);
            if (includeDetails) {
                return Health.status("DEGRADED")
                        .withDetail("DB", "Database is down")
                        .withException(e)
                        .build();
            } else {
                return Health.status("DEGRADED").build();
            }
        }
    }

}
