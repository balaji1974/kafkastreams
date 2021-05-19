package com.bala.kafkastreams.ktableaggregateconsumer.configuration;

import java.util.function.Consumer;

import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.kstream.KStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.bala.kafkastreams.ktableaggregateconsumer.services.RecordBuilder;
import com.bala.kafkastreams.model.Employee;

import lombok.extern.log4j.Log4j2;

@Log4j2
@Configuration
public class KafkaConsumer {
	
	@Autowired
	RecordBuilder recordBuilder;
	
	@Bean
    public Consumer<KStream<String, Employee>> processAggregateRecords() { // This method name is used in application.properties
		Consumer<KStream<String,Employee>> consumer= (KStream<String,Employee> kStream) -> {
			kStream
			.map((k, v) -> KeyValue.pair(v.getId(), v)) // map will take the key value pair from the stream and return a new key which is employee id with the same value 
            .peek((k, v) -> log.info("Key = " + k + " Value = " + v)) // Print this key value pair to our log 
            .toTable() // Convert KStream to KTable 
            .groupBy((k, v) -> KeyValue.pair(v.getDepartment(), v)) // Group it by department name
            .aggregate( // KTable aggregate method takes 3 parameters 
                    () -> recordBuilder.init(), // Initial lambda
                    (k, v, aggV) -> recordBuilder.aggregate(v, aggV), // Adder lambda 
                    (k, v, aggV) -> recordBuilder.subtract(v, aggV) // Subtracter lambda 
            ).toStream() // Convert the record back to a KStream
            .foreach((k, v) -> log.info("Key = " + k + " Value = " + v)); // Print it in the console log 
		};
		return consumer;
    }
    
}
