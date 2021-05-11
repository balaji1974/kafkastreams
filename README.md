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
	java.util.function.Consumer<KStream<String,String>> consumer= (KStream<String,String> kStream) -> {
		kStream.foreach((key, value) -> {
			logger.info("Key: " + key + " Value: " + value);
		});
	};
	return consumer;
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

For more information please look at the official document at:    
https://docs.spring.io/spring-cloud-stream-binder-kafka/docs/3.1.0/reference/html/spring-cloud-stream-binder-kafka.html#_usage    
https://docs.spring.io/spring-cloud-stream-binder-kafka/docs/3.1.0/reference/html/kafka-streams.html    


### 2) Json Pos-Generator (Project: json-pos-generator)    

a. This is an application that will be used for producing data for the rest of this project. It was taken from the below url as it would be easy for us to create streaming data based on this example.    
(Code taken from : https://github.com/LearningJournal/Kafka-Streams-with-Spring-Cloud/tree/main/jsonposgen )    

b. We have 3 data files under the resources\data folder namely:    
invoice.json   
address.json   
products.json   

c. Next we have 3 models for these data sources under the model folder and 3 services under the service folder. A main service called KafkaProducerService.java creates invoice randomly based on the data in the data folder and sends them to the Kafka topic.   


### 3) Avro Pos-Generator (Project: avro-pos-generator)   
 
a. What if your application does not use Json serliazation and uses an Avro serialization? For this we need to register our application to the schema registry to Kafka. Kafka does not provide a schema registery and hence for this purpose we must download the confluence kafka which provides a ready made schema registry for us. After starting our zookeeper and kafka, we can start the confluence schema registry with the following command from the confluence directory:     
bin/schema-registry-start etc/schema-registry/schema-registry.properties     

b. For Avro serilization to auto genreate our avro friendly classes we need to add the following in our pom.xml    
```xml   
<dependency>
    <groupId>org.apache.avro</groupId>
    <artifactId>avro</artifactId>
    <version>1.10.2</version>
</dependency>


<plugin>
	<groupId>org.apache.avro</groupId>
	<artifactId>avro-maven-plugin</artifactId>
	<version>1.9.2</version> <!-- Note: 1.10.x is not working correctly and hence used a lower version -->
	<executions>
		<execution>
			<phase>generate-sources</phase>
			<goals>
				<goal>schema</goal>
			</goals>
			<configuration>
				<sourceDirectory>src/main/avro</sourceDirectory>
				<outputDirectory>${project.build.directory}/generated-sources</outputDirectory>
				<imports>
					<import>${project.basedir}/src/main/avro/LineItem.avsc</import>
					<import>${project.basedir}/src/main/avro/DeliveryAddress.avsc</import>
				</imports>
				<stringType>String</stringType>
			</configuration>
		</execution>
	</executions>
</plugin>
```

c. Next we add the following avro schema description under the avro folder:    
/avro-pos-generator/src/main/avro/DeliveryAddress.avsc    
/avro-pos-generator/src/main/avro/LineItem.avsc    
/avro-pos-generator/src/main/avro/PosInvoice.avsc    

d. Now we are ready to generate the avro friendly classes. From Maven, we need to clean, generate-sources and finally update maven. After this step our avro friendly classes are auto-generated in the /avro-pos-generator/target/generated-sources directory.   

e. We have 3 data files under the resources\data folder namely:    
invoice.json   
address.json   
products.json   

f. Next we add the 3 services under the service folder. A main service called KafkaProducerService.java creates invoice randomly based on the data in the data folder and sends them to the Kafka topic. These two steps are the same as we did for our json project before.    

g. Thats it. Start the project and we can see invoices being produced randomly to our avro-pos-topic topic.    


### 4) Json Pos-Consumer (Project: json-pos-consumer) - Input format: Json & Output format: Avro

a. This project has three requriments:
If the invoice type is "HOME-DELIVERY" then push the invoice to the shipment topic.    
If customer type is "PRIME" then the loyality point is caculated and a notification is sent to the notification topic.    
For all invoives mask the personal information of the customer and send the detials back to the hadoop topic to be later stored into Hadoop for analysis.    
Hence the 3 input topcis will the same "pos-topic" but the output will be to three different topics.   

For this project we will read from our json formatted input data and produce the output in AVRO friendly manner.   

b. For this project we will be using the Kafka streams api which will not use a serializar. But it will need a serde for deserializar purpose which we will add in our dependency.      

```xml   
<dependency>
	<groupId>io.confluent</groupId>
	<artifactId>kafka-streams-avro-serde</artifactId>
	<version>6.1.1</version>
</dependency> 
```
c. Like our last project to create an avro friendly data model we will add our maven dependencies and mavan plugin to create the avro friendly data model.    

d. The above requiremnts is configured in our application.properties as follows:    
spring.cloud.stream.function.definition=processHadoopRecords, processNotificationRecords, processShipmentRecords    
spring.cloud.stream.bindings.processShipmentRecords-in-0.destination=pos-topic    
spring.cloud.stream.bindings.processShipmentRecords-out-0.destination=shipment-topic    
spring.cloud.stream.bindings.processNotificationRecords-in-0.destination=pos-topic    
spring.cloud.stream.bindings.processNotificationRecords-out-0.destination=loyalty-topic    
spring.cloud.stream.bindings.processHadoopRecords-in-0.destination=pos-topic    
spring.cloud.stream.bindings.processHadoopRecords-out-0.destination=hadoop-sink-topic   

spring.cloud.stream.kafka.streams.bindings.processShipmentRecords-out-0.producer.value-serde=io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde    
spring.cloud.stream.kafka.streams.bindings.processNotificationRecords-out-0.producer.value-serde=io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde    
spring.cloud.stream.kafka.streams.bindings.processHadoopRecords-out-0.producer.value-serde=io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde    

e. We will create 3 beans in our Kafka consumer class which will perform the necessary transformation and send back the results to each of the topics. Also for each transformation we have utility methods defined in our RecordBuilder class which does the necessary business logic.    

f. Thats it. Start the producer, and run the consumer to check if the data is properly produced to the 3 topics that we defined in our properties file.     

### 5) Avro Pos-Consumer (Project: avro-pos-consumer) - Input format: Avro & Output format: Json


 








