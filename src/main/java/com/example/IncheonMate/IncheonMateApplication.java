package com.example.IncheonMate;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.mongodb.config.EnableMongoAuditing;

@SpringBootApplication
@EnableMongoAuditing
@EnableFeignClients
public class IncheonMateApplication {

	public static void main(String[] args) {
		SpringApplication.run(IncheonMateApplication.class, args);
	}

}
