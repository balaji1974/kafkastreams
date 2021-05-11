package com.bala.kafkastreams.exactlyonceconsumer.configuration;

import java.util.function.Consumer;

import org.apache.kafka.streams.kstream.KStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.bala.kafkastreams.exactlyonceconsumer.service.RecordBuilder;
import com.bala.kafkastreams.model.HadoopRecord;
import com.bala.kafkastreams.model.Notification;
import com.bala.kafkastreams.model.PosInvoice;

import lombok.extern.log4j.Log4j2;

@Log4j2
@Configuration
public class KafkaConsumer {
	
	@Autowired
	RecordBuilder recordBuilder;
	
	@Value("${hadoop.topic.name}")
	private String HADOOP_TOPIC_NAME;
	
	@Value("${notification.topic.name}")
	private String NOTIFICATION_TOPIC_NAME;
	
	@Bean
    public Consumer<KStream<String, PosInvoice>>  kstreamConsumer() { // This method name is used in application.properties
		Consumer<KStream<String,PosInvoice>> consumer= (KStream<String,PosInvoice> kStream) -> {
			KStream<String, HadoopRecord> hadoopRecordKStream = kStream
	                .mapValues( v -> recordBuilder.getMaskedInvoice(v))
	                .flatMapValues( v -> recordBuilder.getHadoopRecords(v));
			
			KStream<String, Notification> notificationKStream = kStream
	                .filter((k, v) -> v.getCustomerType().equalsIgnoreCase("PRIME"))
	                .mapValues(v -> recordBuilder.getNotification(v));
			
			hadoopRecordKStream.foreach((k, v) -> log.info(String.format("Hadoop Record:- Key: %s, Value: %s", k, v)));
	        notificationKStream.foreach((k, v) -> log.info(String.format("Notification:- Key: %s, Value: %s", k, v)));

	        hadoopRecordKStream.to(HADOOP_TOPIC_NAME);
	        notificationKStream.to(NOTIFICATION_TOPIC_NAME);
			
		};
		
		return consumer;
    }
	
	
	
}
