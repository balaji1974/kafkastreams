package com.bala.kafkastreams.streamlistener.configurations;

import org.apache.kafka.streams.kstream.KStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Consumer {
	
	//https://docs.spring.io/spring-cloud-stream-binder-kafka/docs/3.1.0/reference/html/spring-cloud-stream-binder-kafka.html#_functional_style
	//https://stackoverflow.com/questions/66881210/spring-cloud-stream-kafka-streams-binder-kafkaexception-could-not-start-stream
		
	Logger logger=LoggerFactory.getLogger(Consumer.class);
	
	@Bean
    public java.util.function.Consumer<KStream<String, String>> kstreamConsumer() { // This method name is used in application.properties
        return input ->
        	input.foreach((key, value) -> {
        		logger.info("Key: " + key + " Value: " + value);
            });
    }
}
