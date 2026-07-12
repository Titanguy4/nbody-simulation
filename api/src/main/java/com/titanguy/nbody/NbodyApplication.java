package com.titanguy.nbody;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableIntegration
@EnableScheduling
public class NbodyApplication {

	public static void main(String[] args) {
		SpringApplication.run(NbodyApplication.class, args);
	}

}
