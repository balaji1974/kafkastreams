package com.bala.kafkastreams.kstreamgktablejoins.configuration;

import java.util.function.BiConsumer;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.kstream.GlobalKTable;
import org.apache.kafka.streams.kstream.Grouped;
import org.apache.kafka.streams.kstream.KStream;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.support.serializer.JsonSerde;

import com.bala.kafkastreams.kstreamgktablejoins.model.AdClick;
import com.bala.kafkastreams.kstreamgktablejoins.model.AdInventories;

import lombok.extern.log4j.Log4j2;


@Log4j2
@Configuration
public class KafkaConsumer {
	@Bean
    public  BiConsumer<GlobalKTable<String, AdInventories>, KStream<String, AdClick>>  process() {
		
		return (inventory,  click) -> {
			click.foreach((k, v) -> log.info("Click Key: {}, Value: {}",k, v));

			// Join the click KStream with the GlobalKTable ad inventories
	        click.join(inventory, // first argument which is the joining GlobalKTable
	                (clickKey, clickValue) -> clickKey,  // Second argument which is a key value mapper lambda which will produce a new key if needed for the GKTable 
	                (clickValue, inventoryValue) -> inventoryValue) // Third argument is the value joiner lambda 
	                .groupBy((joinedKey, joinedValue) -> joinedValue.getNewsType(), // Group by the news type
	                        Grouped.with(Serdes.String(),
	                                new JsonSerde<>(AdInventories.class)))
	                .count() // Count the records 
	                .toStream() // Convert it to the final KStream which can later be sent to an output stream (We can use BiFunction instead of BiConsumer for this case 
	                .foreach((k, v) -> log.info("Click Key: {}, Value: {}",k, v)); //Display the results  
			
		};
		
	}
    
}
