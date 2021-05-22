package com.bala.kafkastreams.simpletestconsumer.configuration;

import java.util.function.Function;

import org.apache.kafka.streams.kstream.KStream;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


import lombok.extern.log4j.Log4j2;


@Log4j2
@Configuration
public class KafkaConsumer {
	@Bean
    public  Function<KStream<String, String>,KStream<String, String>>  process() {
		return input -> {
			input.foreach((k,v) -> log.info("Received Input: {}",v));
	        return input.mapValues(v -> v.toUpperCase());
		};
	}
    
}
