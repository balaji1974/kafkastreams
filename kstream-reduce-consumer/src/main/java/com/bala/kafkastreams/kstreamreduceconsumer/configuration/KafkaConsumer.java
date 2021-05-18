package com.bala.kafkastreams.kstreamreduceconsumer.configuration;

import java.util.function.Function;

import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.kstream.KStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.bala.kafkastreams.kstreamreduceconsumer.service.RecordBuilder;
import com.bala.kafkastreams.model.Notification;
import com.bala.kafkastreams.model.PosInvoice;

import lombok.extern.log4j.Log4j2;

@Log4j2
@Configuration
public class KafkaConsumer {
	
	@Autowired
	RecordBuilder recordBuilder;
	
	@Bean
    public Function<KStream<String, PosInvoice>, KStream<String, Notification>> processNotificationRecords() {
		Function<KStream<String, PosInvoice>, KStream<String, Notification>> output =  (KStream<String, PosInvoice> input) -> {
			return input.filter((k, v) -> v.getCustomerType().equalsIgnoreCase("PRIME"))
            .map((k, v) -> new KeyValue<>(v.getCustomerCardNo(), recordBuilder.getNotification(v)))
            .groupByKey()
            .reduce((aggValue, newValue) -> {
                newValue.setTotalLoyaltyPoints(newValue.getEarnedLoyaltyPoints() + aggValue.getTotalLoyaltyPoints());
                return newValue;
            })
            .toStream()
            .peek((k, v) -> log.info(String.format("Notification:- Key: %s, Value: %s", k, v)))
            ;

			//notificationKStream.foreach((k, v) -> log.info(String.format("Notification:- Key: %s, Value: %s", k, v)));
			//return notificationKStream;
		};
		return output;
	}
    
}
