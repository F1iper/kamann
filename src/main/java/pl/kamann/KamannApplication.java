package pl.kamann;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class KamannApplication {

	public static void main(String[] args) {
		SpringApplication.run(KamannApplication.class, args);
	}

}
