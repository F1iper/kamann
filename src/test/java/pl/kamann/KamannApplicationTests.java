package pl.kamann;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import pl.kamann.config.TestDatabaseConfig;

@Slf4j
@SpringBootTest
@Import(TestDatabaseConfig.class)
class KamannApplicationTests {

	@Test
	void contextLoads() {
		log.info("Context loaded, test is working.");
	}
}
