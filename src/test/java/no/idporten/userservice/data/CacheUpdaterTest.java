package no.idporten.userservice.data;

import no.idporten.userservice.config.TestRedisConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import java.time.Instant;

import static java.util.Collections.emptyList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

@SpringBootTest(classes = TestRedisConfig.class, properties = {"spring.data.redis.port=6371"})
@AutoConfigureMockMvc
@DisplayName("When using the userservice")
@ActiveProfiles("test")
class CacheUpdaterTest {

    @MockitoSpyBean
    CacheUpdater cacheUpdater;

    @Autowired
    private CachedUserService userService;

    @Test
    void handleUserCreatedEvent() {
        userService.createUser(createUser("52345678910"));
        verify(cacheUpdater).handleUserCreatedEvent(any());
    }

    @Test
    void handleUserUpdateEvent() {
        IDPortenUser user = userService.createUser(createUser("42345678910"));
        userService.updateUser(user);
        verify(cacheUpdater).handleUserUpdatedEvent(any());
    }

    @Test
    void handleUserDeletedEvent() {
        IDPortenUser user = userService.createUser(createUser("52345678910"));
        userService.deleteUser(user.getId());
        verify(cacheUpdater).handleUserDeletedEvent(any());
    }

    private IDPortenUser createUser(String pid) {
        return new IDPortenUser(null, pid, Instant.now(), Instant.now(), true, null, Instant.now(), null, emptyList(), null);
    }
}