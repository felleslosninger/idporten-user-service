package no.idporten.userservice.data;

import no.idporten.userservice.config.EmbeddedRedisLifecycleConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import java.time.Instant;

import static java.util.Collections.emptyList;
import static java.util.concurrent.TimeUnit.SECONDS;
import static no.idporten.userservice.TestData.randomUser;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertFalse;

@SpringBootTest(classes = EmbeddedRedisLifecycleConfig.class, properties = {"spring.data.redis.port=7546"})
@AutoConfigureMockMvc
@DisplayName("When using the cached userservice")
@ActiveProfiles("test")
class UpdateEidLoginMessagesConsumerTest {

    @MockitoSpyBean
    private UserRepository userRepository;

    @Autowired
    private CachedUserService userService;

    @Test
    public void testSearchForUserExistingUsingCache() {
        IDPortenUser user = createUser("12345678900");
        userService.createUser(user);

        IDPortenUser idPortenUser = userService.searchForUser(user.getPid()).get();
        Instant now = Instant.now();
        Login minid = Login.builder().eidName("MinID").lastLogin(Instant.now()).firstLogin(now).build();
        IDPortenUser userSaved = userService.updateUserWithEid(idPortenUser.getId(), minid);

        await().atMost(5, SECONDS).until(() ->
                !userService.searchForUser(userSaved.getPid()).get().getLogins().isEmpty()
        );

        assertFalse(userService.searchForUser(userSaved.getPid()).get().getLogins().isEmpty());
    }

    private IDPortenUser createUser(String pid) {
        return new IDPortenUser(null, pid, Instant.now(), Instant.now(), true, null, Instant.now(), null, emptyList(), null);
    }

}