# Kafka Streams 

## For all the samples to work please start Apache Kafka with the following commands from the Kafka installation directory: 


bin/zookeeper-server-start.sh config/zookeeper.properties    
bin/kafka-server-start.sh config/server.properties    

### For more details on the above commands please refer to my kafka reposiory.   


# Important KStream methods: ->     
# KStream(key,value pair) ->  Same key gets inserted multiple times     
filter, filterNot    
map, mapValues -> one to one     
flatMap, flatMapValues -> one to many     
forEach, peek -> while call terminated with forEach, peek will return the same KStream value back    
print   
branch, merge -> while branch will split the KStream, merge will join 2 KStreams    
to -> is used to send messages to a Kafka topic    
toTable-> is used to convert KStream to a KTable    
repartition, selectKey, groupBy, groupByKey, join -> Methods for grouping, aggrigation and joining    

# KTable -> Same key get updated and when we send a null value the key gets removed.     

### 1) Stream-Listener (Project: stream-listener) - Using KStreams

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


### 4) Json Pos-Consumer (Project: json-pos-consumer) - Input format: Json & Output format: Avro - Using KStreams    

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

### 5) Avro Pos-Consumer (Project: avro-pos-consumer) - Input format: Avro & Output format: Json - Using KStreams    

a. This project has the same requriments as the previous example. except that instead of Json input we get Avro input format and we must publish back Json format data to the topics.

b. Since most of the requirements are the same, the just need to add one more dependency of formating our output topic to a Json format.    
```xml 
<dependency>
    <groupId>io.confluent</groupId>
    <artifactId>kafka-streams-json-schema-serde</artifactId>
    <version>6.1.1</version>
</dependency>
```

c. Like our last project we will first create our Avro friendly class first.    

d. Next we will create our RecordBuilder service and our Kafka Configuations class (same as before)    

e. Now our only change will be in our properties file, which is as below:    
spring.cloud.stream.kafka.streams.bindings.processNotificationRecords-in-0.consumer.value-serde=io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde    
spring.cloud.stream.kafka.streams.bindings.processNotificationRecords-out-0.producer.value-serde=io.confluent.kafka.streams.serdes.json.KafkaJsonSchemaSerde   
spring.cloud.stream.kafka.streams.bindings.processHadoopRecords-in-0.consumer.value-serde=io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde   
spring.cloud.stream.kafka.streams.bindings.processHadoopRecords-out-0.producer.value-serde=io.confluent.kafka.streams.serdes.json.KafkaJsonSchemaSerde   

This is a mapping of our input (Avro format) and our output (Json format).  

f. Now start the producer and run the consumer to check if everything works fine.    

### 6) exactly-once-consumer (Project: exactly-once-consumer) - Input format: Avro & Output format: Avro - Using KStreams    

a. In our previous example we used 2 different listeners to read from the same topic which is a waste of resource when we can just read one time from the same topic and process our result and then publish it back to two different topics.  This example exactly does that.     

b. Also when we are publishing our results back to two different topics from a single interface, one might fail causing data in-consistency across topics. So we need to bound both of this into a single transactional until. We do this by having both the publishing activties under a single method and adding the following configuration in our application.properties file.      
spring.cloud.stream.kafka.streams.binder.configuration.processing.guarantee: exactly_once      

c. Apart from this all other steps like, using avro files to generate avro schema, and having a RecordBuilder service for our result builder is all the same as the previous example.     

d. The only difference is in our KafkaConsumer class where we now have a single bean returing a void Consumer<KStream<String, PosInvoice>> method, and constructing our hadoopRecordKStream and notificationKStream records before publishing them to their own topics.    


### 7) XML Pos-Consumer (Project: xml-pos-consumer) - Input format: xml & Output format: json - Using KStreams    

a. In this example we will send our input messages in xml format, and send the out back to our topic in json format.    

b. All steps for generating the xml class file is the same as before but the dependency for this is a bit different as given in the pom.xml file:

```xml 
<!-- https://mvnrepository.com/artifact/javax.xml.bind/jaxb-api -->
<dependency>
    <groupId>javax.xml.bind</groupId>
    <artifactId>jaxb-api</artifactId>
</dependency>
<!-- https://mvnrepository.com/artifact/com.sun.xml.bind/jaxb-core -->
<dependency>
    <groupId>com.sun.xml.bind</groupId>
    <artifactId>jaxb-core</artifactId>
    <version>3.0.1</version>
</dependency>
<!-- https://mvnrepository.com/artifact/com.sun.xml.bind/jaxb-impl -->
<dependency>
    <groupId>com.sun.xml.bind</groupId>
    <artifactId>jaxb-impl</artifactId>
    <version>3.0.1</version>
</dependency>
<!-- https://mvnrepository.com/artifact/javax.activation/activation -->
<dependency>
    <groupId>javax.activation</groupId>
    <artifactId>activation</artifactId>
    <version>1.1.1</version>
</dependency>
```

c. And the below maven plugin is used for generating the java source files from the xml files:   
```xml 
<plugin>
	<groupId>org.codehaus.mojo</groupId>
	<artifactId>jaxb2-maven-plugin</artifactId>
	<version>2.5.0</version>
	<executions>
		<execution> 
			<id>xjc</id>
			<goals>
				<goal>xjc</goal>
			</goals>
		</execution>
	</executions>
	<configuration>
		<sources>
			<source>${project.basedir}/src/main/resources/schema</source>
		</sources>
		<packageName>com.bala.kafkastreams.model</packageName>
	</configuration>
</plugin>
```

### 8) KTable Demo (Project: ktable-consumer)  - Using KTable    

a. A KTable differs from the KStream in the way it proceesses messages. It stores the message in a local rocksdb database and sends it to the output stream after a given interval. During this time when repeated key is sent the message is overwritten by the same key, which is the nature of a KTable and hence only the last message gets to the output stream.     

b. We will have our usual dependency for this project which would be as follows:    
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
<dependency>
	<groupId>org.projectlombok</groupId>
	<artifactId>lombok</artifactId>
	<optional>true</optional>
</dependency>
```

c. Next we add the following properties in our application.properties file:   
```xml 
spring.cloud.stream.function.definition=processKTable
spring.cloud.stream.bindings.processKTable-in-0.destination=stock-tick-topic

spring.cloud.stream.kafka.streams.binder.brokers=localhost:9092
#default is 30K milliseconds for records to be stored in the local rocksdb before committing it
spring.cloud.stream.kafka.streams.binder.configuration.commit.interval.ms=10000
# this is the name of the local rocksdb database
spring.cloud.stream.kafka.streams.binder.configuration.state.dir=state-store
# this is the name of the Ktable inside the local rocksdb database
spring.cloud.stream.kafka.streams.bindings.processKTable-in-0.consumer.materialized-as=stock-input-store
```

d. Finally we process our incomming message using the processKTable() function inside the KafkaConsumer class where we filter out all messages other than the 'HDFCBANK' message.   

e. We can test our consumer using a producer console with few sample data as follows:   
HDFCBANK:2120    
HDFCBANK:2150    
HDFCBANK:2180    
    
TCS:2920    

e. This is a simple example to show how a KTable functions.    












