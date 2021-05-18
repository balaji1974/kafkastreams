package com.bala.kafkastreams.kstreamaggregateconsumer.configuration;

import java.util.function.Consumer;

import org.apache.kafka.streams.kstream.KStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.bala.kafkastreams.kstreamaggregateconsumer.services.RecordBuilder;
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
			.peek((k, v) -> log.info("Key: {}, Value:{}", k, v)) // Peek at the incoming key-value and log it. 
            .groupBy((k, v) -> v.getDepartment()) // Group by department id 
            .aggregate( // This is the combination of Map and Reduce that was used in the previous example 
                    () -> recordBuilder.init(), // first argument which is the initializer lambda takes the initial state of the aggregator 
                    (k, v, aggV) -> recordBuilder.aggregate(v, aggV) // second argument which is the aggregator lambda which aggregates the value
            ).toStream() //Convert it to a key stream 
            .foreach((k, v) -> log.info("Key = " + k + " Value = " + v.toString())); // Finally loop and print it 
		};
		return consumer;
    }
    
}
