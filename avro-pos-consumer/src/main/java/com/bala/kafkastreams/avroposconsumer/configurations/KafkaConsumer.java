package com.bala.kafkastreams.avroposconsumer.configurations;

import java.util.function.Function;

import org.apache.kafka.streams.kstream.KStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.bala.kafkastreams.avroposconsumer.model.HadoopRecord;
import com.bala.kafkastreams.avroposconsumer.model.Notification;

import com.bala.kafkastreams.avroposconsumer.service.RecordBuilder;
import com.bala.kafkastreams.model.PosInvoice;

import lombok.extern.log4j.Log4j2;

@Log4j2
@Configuration
public class KafkaConsumer {
	
	@Autowired
	RecordBuilder recordBuilder;
	
	@Bean
    public Function<KStream<String, PosInvoice>, KStream<String, Notification>> processNotificationRecords() {
        return input -> input
                .filter((k, v) -> v.getCustomerType().equalsIgnoreCase("PRIME"))
                .mapValues(v -> recordBuilder.getNotification(v))
                .peek((k, v) -> log.info(String.format("Notification Record:- Key: %s, Value: %s", k, v)));
    }

	@Bean
    public Function<KStream<String, PosInvoice>, KStream<String, HadoopRecord>> processHadoopRecords() {
        return input -> input
        		.mapValues( v -> recordBuilder.getMaskedInvoice(v))
        		.flatMapValues( v -> recordBuilder.getHadoopRecords(v))
                .peek((k, v) -> log.info(String.format("Hadoop Record:- Key: %s, Value: %s", k, v)));
    }
}
