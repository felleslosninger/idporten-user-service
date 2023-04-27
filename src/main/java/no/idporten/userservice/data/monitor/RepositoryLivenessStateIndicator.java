package no.idporten.userservice.data.monitor;

import lombok.extern.slf4j.Slf4j;
import no.idporten.userservice.data.UserRepository;
import org.springframework.boot.actuate.availability.LivenessStateHealthIndicator;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.availability.ApplicationAvailability;
import org.springframework.stereotype.Component;

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
    public Health getHealth(boolean includeDetails) {
        try {
            // TODO: Temp. disabled, see JIRA-issue ID-2800
            //userRepository.count();
            return Health.up().build();
        } catch (Exception e) {
            log.error("Health check to repository failed with exception " + e.getMessage(), e);
            if (includeDetails) {
                return Health.up().withException(e).build();
            } else {
                return Health.up().build();
            }
        }
    }

}
