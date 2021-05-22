package com.bala.kafkastreams.topthreejoinconsumer.configuration;

import java.util.function.BiConsumer;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.kstream.GlobalKTable;
import org.apache.kafka.streams.kstream.Grouped;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.state.KeyValueStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.support.serializer.JsonSerde;

import com.bala.kafkastreams.topthreejoinconsumer.model.AdClick;
import com.bala.kafkastreams.topthreejoinconsumer.model.AdInventories;
import com.bala.kafkastreams.topthreejoinconsumer.model.ClicksByNewsType;
import com.bala.kafkastreams.topthreejoinconsumer.model.Top3NewsTypes;
import com.fasterxml.jackson.core.JsonProcessingException;

import lombok.extern.log4j.Log4j2;


@Log4j2
@Configuration
public class KafkaConsumer {
	@Bean
    public  BiConsumer<GlobalKTable<String, AdInventories>, KStream<String, AdClick>>  process() {
		
		 return (inventory,  click) -> {
			click.foreach((k, v) -> log.info("Click Key: {}, Value: {}",k, v));
			
			// Same as last example until this point where an intermediate KTable is created with the results 
			KTable<String, Long> clicksByNewsTypeKTable = click.join(inventory,
	                (clickKey, clickValue) -> clickKey,
	                (clickValue, inventoryValue) -> inventoryValue)
	                .groupBy((joinedKey, joinedValue) -> joinedValue.getNewsType(),
	                        Grouped.with(Serdes.String(),
	                                new JsonSerde<>(AdInventories.class)))
	                .count();
			
			// This group by will bring all the distributed data across different KTables into a single area 
			// with op3NewsTypes as key and value as the original key and its value 
	        clicksByNewsTypeKTable.groupBy(
	                (k, v) -> {
	                    ClicksByNewsType value = new ClicksByNewsType();
	                    value.setNewsType(k);
	                    value.setClicks(v);
	                    return KeyValue.pair("top3NewsTypes", value);
	                },
	                Grouped.with(Serdes.String(), new JsonSerde<>(ClicksByNewsType.class))
	        )
	         // default value, adder and subtrator as parameters
	         .aggregate(
	                () -> new Top3NewsTypes(),
	                (k, v, aggV) -> {
	                    aggV.add(v);
	                    return aggV;
	                },
	                (k, v, aggV) -> {
	                    aggV.remove(v);
	                    return aggV;
	                },
	                // Default Serdes 
	                Materialized.<String, Top3NewsTypes, KeyValueStore<Bytes, byte[]>>
	                        as("top3-clicks")
	                        .withKeySerde(Serdes.String())
	                        .withValueSerde(new JsonSerde<>(Top3NewsTypes.class)))
	         // Finally to stream to be sent to the ouput stream or in our case to the console log
	         .toStream().foreach(
	                (k, v) -> {
	                    try {
	                        log.info("k=" + k + " v= " + v.getTop3Sorted());
	                    } catch (JsonProcessingException e) {
	                        e.printStackTrace();
	                    }
	                });

		};
		
	}
    
}
