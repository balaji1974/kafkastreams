package com.bala.kafkastreams.exactlyonceconsumer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ExactlyOnceConsumerApplication {

	public static void main(String[] args) {
		SpringApplication.run(ExactlyOnceConsumerApplication.class, args);
	}

}
