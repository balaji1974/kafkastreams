package com.bala.kafkastreams.xmlposconsumer.configuration;

import java.io.StringReader;
import java.util.function.Function;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Predicate;
import org.apache.kafka.streams.kstream.Produced;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.bala.kafkastreams.model.Order;
import com.bala.kafkastreams.xmlposconsumer.model.OrderEnvelop;

import lombok.extern.log4j.Log4j2;



@Log4j2
@Configuration
public class KafkaConsumer {
	
	@Value("${error.topic.name}")
    private String ERROR_TOPIC;
	
	@Value("${india-orders.topic.name}")
    private String INDIA_ORDER_TOPIC;
	
	@Value("${abroad-orders.topic.name}")
    private String ABROAD_ORDER_TOPIC;
	
	@Bean
	public Function<KStream<String, String>, KStream<String, Order>[]> kstreamConsumer() {
		
		Function<KStream<String, String>, KStream<String, Order>[]> output =  (KStream<String, String> input) -> {
			input.foreach((k, v) -> log.info(String.format("Received XML Order Key: %s, Value: %s", k, v)));
			KStream<String, OrderEnvelop> orderEnvelopKStream = input.map((key, value) -> {
	            OrderEnvelop orderEnvelop = new OrderEnvelop();
	            orderEnvelop.setXmlOrderKey(key);
	            orderEnvelop.setXmlOrderValue(value);
	            try {
	                JAXBContext jaxbContext = JAXBContext.newInstance(Order.class);
	                Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();

	                orderEnvelop.setValidOrder((Order) jaxbUnmarshaller.unmarshal(new StringReader(value)));
	                orderEnvelop.setOrderTag(AppConstants.VALID_ORDER);

	                if(orderEnvelop.getValidOrder().getShipTo().getCity().isEmpty()){
	                    log.error("Missing destination City");
	                    orderEnvelop.setOrderTag(AppConstants.ADDRESS_ERROR);
	                }

	            } catch (JAXBException e) {
	                log.error("Failed to Unmarshal the incoming XML");
	                orderEnvelop.setOrderTag(AppConstants.PARSE_ERROR);
	            }
	            return KeyValue.pair(orderEnvelop.getOrderTag(), orderEnvelop);
	        });

	        orderEnvelopKStream.filter((k, v) -> !k.equalsIgnoreCase(AppConstants.VALID_ORDER))
	                .to(ERROR_TOPIC, Produced.with(AppSerdes.String(), AppSerdes.OrderEnvelop()));

	        KStream<String, Order> validOrders = orderEnvelopKStream
	                .filter((k, v) -> k.equalsIgnoreCase(AppConstants.VALID_ORDER))
	                .map((k, v) -> KeyValue.pair(v.getValidOrder().getOrderId(), v.getValidOrder()));

	        validOrders.foreach((k, v) -> log.info(String.format("Valid Order with ID: %s", v.getOrderId())));

	        Predicate<String, Order> isIndiaOrder = (k, v) -> v.getShipTo().getCountry().equalsIgnoreCase("india");
	        Predicate<String, Order> isAbroadOrder = (k, v) -> !v.getShipTo().getCountry().equalsIgnoreCase("india");
	        
	        
	        KStream<String, Order>[] orders=validOrders.branch(isIndiaOrder, isAbroadOrder);
	        orders[0].to(INDIA_ORDER_TOPIC);
	        orders[1].to(ABROAD_ORDER_TOPIC);
	        return orders;
			
		};
		return output;
		
	    
	}
}