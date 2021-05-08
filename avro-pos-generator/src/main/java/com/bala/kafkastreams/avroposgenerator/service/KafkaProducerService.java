package com.bala.kafkastreams.avroposgenerator.service;


import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.bala.kafkastreams.avroposgenerator.model.PosInvoice;

@Service
@Log4j2
public class KafkaProducerService {
    @Value("${application.configs.topic.name}")
    private String TOPIC_NAME;

    @Autowired
    private KafkaTemplate<String, PosInvoice> kafkaTemplate;

    public void sendMessage(PosInvoice invoice) {
        log.info(String.format("Producing Invoice No: %s Customer Type: %s",
                invoice.getInvoiceNumber(),
                invoice.getCustomerType()));
        kafkaTemplate.send(TOPIC_NAME, invoice.getStoreID(), invoice);
    }
}
