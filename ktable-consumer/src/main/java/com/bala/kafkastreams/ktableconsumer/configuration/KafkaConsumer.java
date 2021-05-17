package com.bala.kafkastreams.ktableconsumer.configuration;

import org.apache.kafka.streams.kstream.KTable;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import lombok.extern.log4j.Log4j2;

import java.util.function.Consumer;

@Log4j2
@Configuration
public class KafkaConsumer {
	
	
	/* Sample Data
	 * HDFCBANK:2120
	 * HDFCBANK:2150
	 * HDFCBANK:2180
	 *	
	 * TCS:2920
	 */
	
	@Bean
    public Consumer<KTable<String, String>> processKTable() { // This method name is used in application.properties
		Consumer<KTable<String,String>> consumer= (KTable<String,String> kTable) -> {
			kTable.filter((key, value) -> key.contains("HDFCBANK")) // Code to filter out all other records and accept only HDFCBANK records
            .toStream()
            .foreach((k, v) -> log.info("Key: " + k + " Value: " + v));
		};
		return consumer;
    }
	
}
