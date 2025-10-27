package no.idporten.userservice.data.monitor;

import no.idporten.userservice.data.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.availability.ApplicationAvailability;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RepositoryLivenessStateIndicatorTest {

    private final UserRepository userRepository = mock(UserRepository.class);
    private final ApplicationAvailability  applicationAvailability = mock(ApplicationAvailability.class);
    private final RepositoryLivenessStateIndicator repositoryLivenessStateIndicator = new  RepositoryLivenessStateIndicator(applicationAvailability, userRepository);

    @Test
    void reportUpWhenDatabaseIsUp() {
        Health health = repositoryLivenessStateIndicator.getHealth(true);
        assertEquals(Health.up().build(), health);
    }

    @Test
    void reportDegradedWhenDatabaseIsDown() {
        when(userRepository.findByUuid(any())).thenThrow(new RuntimeException());
        Health health = repositoryLivenessStateIndicator.getHealth(true);
        assertEquals(Health.status("DEGRADED").build().getStatus().toString(), health.getStatus().toString());
    }

}