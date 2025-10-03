package no.idporten.userservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class IdportenUserServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(IdportenUserServiceApplication.class, args);
	}

}
