package com.bala.kafkastreams.avroposgenerator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.bala.kafkastreams.avroposgenerator.service.KafkaProducerService;
import com.bala.kafkastreams.avroposgenerator.service.datagenerator.InvoiceGenerator;

@SpringBootApplication
public class AvroPosGeneratorApplication implements ApplicationRunner{
	
	@Autowired
    private KafkaProducerService producerService;

    @Autowired
    private InvoiceGenerator invoiceGenerator;
    
    @Value("${application.configs.invoice.count}")
    private int INVOICE_COUNT;

	public static void main(String[] args) {
		SpringApplication.run(AvroPosGeneratorApplication.class, args);
	}
	
 	@Override
    public void run(ApplicationArguments args) throws Exception {

        for (int i = 0; i < INVOICE_COUNT; i++) {
            producerService.sendMessage(invoiceGenerator.getNextInvoice());
            Thread.sleep(500);
        }
    }


}
