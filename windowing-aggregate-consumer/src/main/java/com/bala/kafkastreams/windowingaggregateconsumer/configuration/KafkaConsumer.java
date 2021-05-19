package com.bala.kafkastreams.windowingaggregateconsumer.configuration;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.function.Consumer;

import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.TimeWindows;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.bala.kafkastreams.windowingaggregateconsumer.model.SimpleInvoice;

import lombok.extern.log4j.Log4j2;

@Log4j2
@Configuration
public class KafkaConsumer {

	@Bean
    public Consumer<KStream<String, SimpleInvoice>> processWindowAggregateRecords() { // This method name is used in application.properties
		Consumer<KStream<String,SimpleInvoice>> consumer= (KStream<String,SimpleInvoice> kStream) -> {
			kStream
			.peek((k, v) -> log.info("Key = " + k + " Created Time = " 
	                + Instant.ofEpochMilli(v.getCreatedTime()).atOffset(ZoneOffset.UTC))) // Printing the value which is the created time got because of the timestamp-extractor-bean-name 
	                .groupByKey() // Record is grouped by store id which is the key 
	                .windowedBy(TimeWindows.of(Duration.ofMinutes(5))) // subgroup by a 5 minute time window 
	                .count() // count the records which results in a KTable
	                .toStream() // convert the records into a KStream
	                .foreach((k, v) -> log.info(    // Print the result 
	                        "StoreID: " + k.key() +
	                                " Window start: " +
	                                Instant.ofEpochMilli(k.window().start())
	                                        .atOffset(ZoneOffset.UTC) +
	                                " Window end: " +
	                                Instant.ofEpochMilli(k.window().end())
	                                        .atOffset(ZoneOffset.UTC) +
	                                " Count: " + v +
	                                " Window#: " + k.window().hashCode()
	                ));
		};
		return consumer;
    }
    
}
