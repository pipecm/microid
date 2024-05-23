package com.simplyfelipe.microid;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
class MicroidApplicationTests extends ApplicationTest {

	@Test
	void contextLoads() {
		assertThat(1).isEqualTo(1);
	}

}
