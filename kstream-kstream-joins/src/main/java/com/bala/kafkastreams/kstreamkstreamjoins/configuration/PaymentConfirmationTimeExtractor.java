package com.bala.kafkastreams.kstreamkstreamjoins.configuration;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.streams.processor.TimestampExtractor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.bala.kafkastreams.kstreamkstreamjoins.model.PaymentConfirmation;

@Configuration
public class PaymentConfirmationTimeExtractor implements TimestampExtractor{

    @Override
    public long extract(ConsumerRecord<Object, Object> consumerRecord, long prevTime) {
        PaymentConfirmation confirmation = (PaymentConfirmation) consumerRecord.value();
        return ((confirmation.getCreatedTime() > 0) ? confirmation.getCreatedTime() : prevTime);
    }

    @Bean
    public TimestampExtractor confirmationTimeExtractor() {
        return new PaymentConfirmationTimeExtractor();
    }
}
