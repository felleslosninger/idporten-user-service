package no.idporten.userservice.data;

import no.idporten.userservice.config.TestRedisConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest(classes = TestRedisConfig.class)
@AutoConfigureMockMvc
@DisplayName("When using the userservice")
@ActiveProfiles("test")
public class UserServiceIntegrationTest {

    @MockitoSpyBean
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @Test
    public void testSearchForUserExistingUsingCache() {
        userService.createUser(createUser("12345678910", UUID.randomUUID()));
        Optional<IDPortenUser> idPortenUser = userService.searchForUser("12345678910");
        assertTrue(idPortenUser.isPresent());
        verify(userRepository, times(1)).findByPersonIdentifier(anyString());
    }

    @Test
    public void testSearchForNonExistingUserUsingCache() {
        userService.createUser(createUser("12345678911", UUID.randomUUID()));
        Optional<IDPortenUser> idPortenUser = userService.searchForUser("12345678915");
        assertFalse(idPortenUser.isPresent());
        verify(userRepository, times(2)).findByPersonIdentifier(anyString());
    }



    private IDPortenUser createUser(String pid, UUID uuid) {
        return new IDPortenUser(null, pid, Instant.now(), Instant.now(), true, null, Instant.now(), null, emptyList(), null, false);
    }
}
