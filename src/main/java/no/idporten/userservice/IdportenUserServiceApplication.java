package no.idporten.userservice;

import no.idporten.logging.access.AccessLogsConfiguration;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import({ AccessLogsConfiguration.class})
public class IdportenUserServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(IdportenUserServiceApplication.class, args);
	}

}
