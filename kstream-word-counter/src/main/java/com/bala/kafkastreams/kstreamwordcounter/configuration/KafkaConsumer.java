package com.bala.kafkastreams.kstreamwordcounter.configuration;

import org.apache.kafka.streams.kstream.KStream;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import lombok.extern.log4j.Log4j2;

import java.util.Arrays;
import java.util.function.Consumer;

@Log4j2
@Configuration
public class KafkaConsumer {
	
	@Bean
    public Consumer<KStream<String, String>> processKStream() { // This method name is used in application.properties
		Consumer<KStream<String,String>> consumer= (KStream<String,String> kStream) -> {
			KStream<String, String> wordStream = kStream
	                .flatMapValues(value -> Arrays.asList(value.toLowerCase().split(" ")));

	        wordStream.groupBy((key, value) -> value)
	                .count()
	                .toStream()
	                .peek((k, v) -> log.info("Word: {} Count: {}", k, v));
		};
		return consumer;
    }
	
}
