package com.example.data;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles(profiles = {"local",  "secret"})
@SpringBootTest
class DataApplicationTests {

	@Test
	void contextLoads() {
	}

}
