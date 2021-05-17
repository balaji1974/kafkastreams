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
	 * EDMUNDKNIFE:10500
	 * EDMUNDKNIFE:10700
	 * EDMUNDKNIFE:12000
	 *	
	 * NEWLANDPAINTING:95000
	 */
	
	@Bean
    public Consumer<KTable<String, String>> processKTable() { // This method name is used in application.properties
		Consumer<KTable<String,String>> consumer= (KTable<String,String> kTable) -> {
			kTable.filter((key, value) -> key.contains("EDMUNDKNIFE")) // Code to filter out all other records and accept only EDMUNDKNIFE record
            .toStream()
            .foreach((k, v) -> log.info("Key: " + k + " Value: " + v));
		};
		return consumer;
    }
	
}
