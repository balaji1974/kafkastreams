package com.bala.kafkastreams.streamlistener.configurations;

import org.apache.kafka.streams.kstream.KStream;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import lombok.extern.log4j.Log4j2;

import java.util.function.Consumer;

@Log4j2
@Configuration
public class KafkaConsumer {
	
	//https://docs.spring.io/spring-cloud-stream-binder-kafka/docs/3.1.0/reference/html/spring-cloud-stream-binder-kafka.html#_functional_style
	//https://stackoverflow.com/questions/66881210/spring-cloud-stream-kafka-streams-binder-kafkaexception-could-not-start-stream
	
	@Bean
    public Consumer<KStream<String, String>> kstreamConsumer() { // This method name is used in application.properties
		Consumer<KStream<String,String>> consumer= (KStream<String,String> kStream) -> {
			kStream.foreach((key, value) -> {
				log.info("Key: " + key + " Value: " + value);
			});
		};
		return consumer;
    }
	
}
