package com.example.data;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
// @EnableJpaAuditing
public class DataApplication {

	// @PostConstruct
	// public void started() {
	// 	TimeZone.setDefault(TimeZone.getTimeZone("Asia/Seoul"));
	// }

	public static void main(String[] args) {
		SpringApplication.run(DataApplication.class, args);
	}

}
