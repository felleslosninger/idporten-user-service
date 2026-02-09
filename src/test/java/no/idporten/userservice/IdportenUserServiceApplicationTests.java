package no.idporten.userservice;

import no.idporten.userservice.config.EmbeddedRedisLifecycleConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(classes = EmbeddedRedisLifecycleConfig.class, properties = {"spring.data.redis.port=7551"})
@ActiveProfiles("test")
class IdportenUserServiceApplicationTests {

	@Test
	void contextLoads() {
	}

}
