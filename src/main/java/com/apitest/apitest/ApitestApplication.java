package com.apitest.apitest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = "com.apitest")
@EnableScheduling
public class ApitestApplication {

	public static void main(String[] args) {
		SpringApplication.run(ApitestApplication.class, args);
	}

}
