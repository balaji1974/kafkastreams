# Kafka Streams 

## For all the samples to work please start Apache Kafka with the following commands from the Kafka installation directory: 


bin/zookeeper-server-start.sh config/zookeeper.properties    
bin/kafka-server-start.sh config/server.properties    

### For more details on the above commands please refer to my kafka reposiory.   


### 1) Stream-Listener (Project: stream-listener)

a. Add the following dependencies in the pom.xml:    
```xml
<dependency>
	<groupId>org.apache.kafka</groupId>
	<artifactId>kafka-streams</artifactId>
</dependency>
<dependency>
	<groupId>org.springframework.cloud</groupId>
	<artifactId>spring-cloud-stream</artifactId>
</dependency>
<dependency>
	<groupId>org.springframework.cloud</groupId>
	<artifactId>spring-cloud-stream-binder-kafka-streams</artifactId>
</dependency>
```

b. Next create a consumer class with the following bean: 
```
@Bean
public java.util.function.Consumer<KStream<String, String>> kstreamConsumer() { // This method name is used in application.properties
    return input ->
    	input.foreach((key, value) -> {
    		logger.info("Key: " + key + " Value: " + value);
        });
}
```
Starting from Java 8 we can concisely represented as a lambda expression of type java.util.function.Consumer.  The application consumes data and logs the information from the KStream key and value on the standard output. The bean method is of type java.util.function.Consumer which is parameterized with KStream. Then in the implementation, we are returning a Consumer object that is essentially a lambda expression. Inside the lambda expression, the code for processing the data is provided.    

c. Also note that in the above code the binder creates the binding for the application with a name kstreamConsumer-in-0, i.e. the name of the function bean name followed by a dash character (-) and the literal in followed by another dash and then the ordinal position of the parameter.    

d. In the application.properties file, add the following settings to glue everything together.   
\#Consumer bean function name     
spring.cloud.stream.function.definition=kstreamConsumer    
\#Binding destination to topic     
spring.cloud.stream.bindings.kstreamConsumer-in-0.destination=my_sample_consumer    

\#Application id    
spring.cloud.stream.kafka.streams.binder.application-id=stream-listener    
\#Broker url     
spring.cloud.stream.kafka.streams.binder.brokers=localhost:9092    
\#KStream key type     
spring.cloud.stream.kafka.streams.binder.configuration.default.key.serde=org.apache.kafka.common.serialization.Serdes$StringSerde   
\#KStream value type     
spring.cloud.stream.kafka.streams.binder.configuration.default.value.serde=org.apache.kafka.common.serialization.Serdes$StringSerde    

e. With this start the application and create a producer from the command line with the following command:     
kafka-console-producer --broker-list localhost:9092 --topic my_sample_consumer    

f. Now when we type anything in the producer it gets displayed in the consumer. (Note: the topic is autocreated when the application starts, but this depends on the kafka settings)    


