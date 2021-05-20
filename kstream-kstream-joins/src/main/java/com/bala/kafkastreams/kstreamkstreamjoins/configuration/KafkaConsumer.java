package com.bala.kafkastreams.kstreamkstreamjoins.configuration;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.function.BiConsumer;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.kstream.JoinWindows;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.StreamJoined;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.support.serializer.JsonSerde;

import com.bala.kafkastreams.kstreamkstreamjoins.model.PaymentConfirmation;
import com.bala.kafkastreams.kstreamkstreamjoins.model.PaymentRequest;
import com.bala.kafkastreams.kstreamkstreamjoins.service.RecordBuilder;

import lombok.extern.log4j.Log4j2;

@Log4j2
@Configuration
public class KafkaConsumer {
	
	@Autowired
    private RecordBuilder recordBuilder;

	@Bean
    public  BiConsumer<KStream<String, PaymentRequest>, KStream<String, PaymentConfirmation>>  process() {
		
		return (paymentRequest, paymentConfirmation) -> {
			
			paymentRequest.foreach((k, v) -> log.info("Request Key = " + k + " Created Time = "
	                + Instant.ofEpochMilli(v.getCreatedTime()).atOffset(ZoneOffset.UTC)));
			
			paymentConfirmation.foreach((k, v) -> log.info("Confirmation Key = " + k + " Created Time = "
	                + Instant.ofEpochMilli(v.getCreatedTime()).atOffset(ZoneOffset.UTC)));
			
			
			/*
			 * Join method takes 3 mandatory arguments 
			 * Both join keys are the same, as both messages come with the transaction id as the key
			 * We must also create both the topics with the same number of partitions 
			 */
			paymentRequest.join(
					// First argument is the other stream data to be joined
					paymentConfirmation,  
					// Second argument is the value lambda argument which takes 2 arguments 
					// the payment request and confirmation and returns the transaction status 
					// The framework takes cares of sending the matching records  
	                (r, c) -> recordBuilder.getTransactionStatus(r, c), 
	                // Third argument is the join window for the matching records 
	                JoinWindows.of(Duration.ofMinutes(5)), 
	                // This argument is for defining the required serdes since sometime join operation cannot determine it automatically 
	                StreamJoined.with(Serdes.String(), // This is common key between the 2 input streams 
	                        new JsonSerde<>(PaymentRequest.class), // This is the serde for the value of the left side of the join 
	                        new JsonSerde<>(PaymentConfirmation.class))) // This is the serde for the value of the right side of the join 
					// Finally iterate and print the transaction status which in real scenario will be sending to to another listening output topic 
	                .foreach((k, v) -> log.info("Transaction ID = " + k + " Status = " + v.getStatus())); 
		};
		
	}
    
}
