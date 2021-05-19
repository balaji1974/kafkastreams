# Kafka Streams 

## For all the samples to work please start Apache Kafka with the following commands from the Kafka installation directory: 


bin/zookeeper-server-start.sh config/zookeeper.properties    
bin/kafka-server-start.sh config/server.properties    

### For more details on the above commands please refer to my kafka reposiory.   

    
# KStream(key,value pair)    
##Same key gets inserted multiple times     

# KTable(key,value pair)     
## Same key get updated and when we send a null value the key gets removed. Null key is not permitted here. Also data gets stored in an intermediate RocksDB database until the commit interval and hence same key sent during this period will send only the last key-value pair to the output stream.    

## Important KStream methods: ->     
filter, filterNot    
map, mapValues -> one to one     
flatMap, flatMapValues -> one to many     
forEach, peek -> while call terminated with forEach, peek will return the same KStream value back    
print   
branch, merge -> while branch will split the KStream, merge will join 2 KStreams    
to -> is used to send messages to a Kafka topic    
toTable-> is used to convert KStream to a KTable    
repartition, selectKey, groupBy, groupByKey, join -> Methods for grouping, aggrigation and joining    

## Grouping Methods    
KStream -> groupBy(), groupByKey()    
KTable -> groupBy()    

## Aggregation Methods    
count()    
reduce()    
aggregate()    

## Key Preserving APIs is preferred    
mapValues(), flatMapValues(), transformValues(), groupByKey()     

## Key Changing APIs is not preferred as it will cause data to shuffle sort which is an expensive operation    
map(), flatMap(), transform(), groupBy()     


## Time Schematics    
Event Time - The time when the event was produced (Custom Timestamp extractor must be used for this)     
Ingestion Time - The time when the record reached the broker (this can be got by setting the flag message.timestamp.type=LogAppendTime) (Extractor that can be used - FailOnInvalidTimestamp, LogAndSkipOnInvalidTimestamp, UsePreviousTimeOnInvalidTimestamp) 
Processing Time - The time the message was processed by the broker (Extractor to use - WallclockTimestampExtractor)     


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
spring.cloud.stream.bindings.processKTable-in-0.destination=auction-price-ticker


spring.cloud.stream.kafka.streams.binder.brokers=localhost:9092
#default is 30K milliseconds for records to be stored in the local rocksdb before committing it
spring.cloud.stream.kafka.streams.binder.configuration.commit.interval.ms=10000
# this is the name of the local rocksdb database
spring.cloud.stream.kafka.streams.binder.configuration.state.dir=application-state-store
# this is the name of the Ktable inside the local rocksdb database
spring.cloud.stream.kafka.streams.bindings.processKTable-in-0.consumer.materialized-as=auction-price-store
```

d. Finally we process our incomming message using the processKTable() function inside the KafkaConsumer class where we filter out all messages other than the 'EDMUNDKNIFE' message.   

e. We can test our consumer using a producer console with few sample data as follows:   
Create a topic -     
bin/kafka-topics.sh --create --zookeeper localhost:2181 --replication-factor 1 --partitions 1 --topic auction-price-ticker           
    
Create a console producer -    
bin/kafka-console-producer.sh --topic auction-price-ticker --broker-list localhost:9092 --property parse.key=true --property key.separator=":"       

Send sample messages -     
EDMUNDKNIFE:10500    
EDMUNDKNIFE:10700    
EDMUNDKNIFE:12000    
    
NEWLANDPAINTING:95000    

f. Start the application and check the result. This is a simple example to show how a KTable functions.    

### 9) Streaming Word Counter (Project: kstream-word-counter)  - Using KStream    
a. This project is used to count the number of words in our stream of data.    

b. All dependencies for this project are the same as before.    

c. Next we add the following properties in our application.properties file:   
```xml 
spring.cloud.stream.function.definition=processKStream
spring.cloud.stream.bindings.processKStream-in-0.destination=streaming-word-counter

spring.cloud.stream.kafka.streams.binder.brokers=localhost:9092
#default is 30K milliseconds for records to be stored in the local rocksdb before committing it
spring.cloud.stream.kafka.streams.binder.configuration.commit.interval.ms=10000
# this is the name of the local rocksdb database
spring.cloud.stream.kafka.streams.binder.configuration.state.dir=application-state-store
spring.cloud.stream.kafka.streams.binder.configuration.default.key.serde=org.apache.kafka.common.serialization.Serdes$StringSerde
spring.cloud.stream.kafka.streams.binder.configuration.default.value.serde=org.apache.kafka.common.serialization.Serdes$StringSerde   
```

d. Next we process our incomming message using the processKStream() function inside the KafkaConsumer class where we first split our words into a null value key and the word as the value of the KStream object. Next we use the groupBy function were the values are exchanged as keys and count applied on the value. The result is a KTable which is then converted to a KStream object which is then sent to the output stream or printed.    

e. We can test our consumer using a producer console with few sample data as follows:   
Create a topic -     
bin/kafka-topics.sh --create --zookeeper localhost:2181 --replication-factor 1 --partitions 1 --topic streaming-word-counter           
    
Create a console producer -    
bin/kafka-console-producer.sh --topic streaming-word-counter --broker-list localhost:9092        

Send sample messages -     
Hello Balaji    
How are you doing Balaji    
How are things at your end?      

f. Start the application and check the count result.    

### 10) Calculating the total from a stream of data (Project: kstream-reduce-consumer)  - Input format: avro & Output format: avro - Using KStream's reduce function.   
a. This project is used to calculate the total loyality points from a stream of data that comes from a Kafka producer.     

b. We use our Avro POS generator to generate data for this consumer to consume.    

c. We genrate Avro classes for the given 4 avro objects. Next we set our configuration in our application.properties files as follows. This is fairly straight forward and discussed in length before:     
```xml 
spring.cloud.stream.function.definition=processNotificationRecords
spring.cloud.stream.bindings.processNotificationRecords-in-0.destination=avro-pos-topic
spring.cloud.stream.bindings.processNotificationRecords-out-0.destination=loyalty-topic

spring.cloud.stream.kafka.streams.binder.brokers=localhost:9092
spring.cloud.stream.kafka.streams.binder.configuration.schema.registry.url=http://localhost:8081
spring.cloud.stream.kafka.streams.binder.configuration.commit.interval.ms=10000
spring.cloud.stream.kafka.streams.binder.configuration.state.dir=application-state-store
spring.cloud.stream.kafka.streams.binder.configuration.default.key.serde=org.apache.kafka.common.serialization.Serdes$StringSerde
spring.cloud.stream.kafka.streams.binder.configuration.default.value.serde=io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde
```

d. Finally we create our Kafka configuration class that receives the stream of POS invoice data from the input channel, convert it to the output Notification object format, compute the accmulated loayalty points,  prints the accumulated points, and finally the sends the output data to the output topic.    

e. The function processNotificationRecords() is self expalinatory with all the comments.    

### 11) Calculating the average from a stream of data (Project: kstream-aggregate-consumer)  - Input format: avro & Output format: avro - Using KStream's aggregate function.  
a. This project is used to calculate the average departmenet salaries of employees from a stream of data that comes from a Kafka producer (AVRO format)    

b. We need the following dependencies in our pom.xml    
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
<dependency>
    <groupId>org.apache.avro</groupId>
    <artifactId>avro</artifactId>
    <version>1.10.2</version>
</dependency>
<dependency>
	<groupId>io.confluent</groupId>
	<artifactId>kafka-streams-avro-serde</artifactId>
	<version>6.1.1</version>
</dependency>
```

c. We generate the Java friendly avro classes with the maven plugin:    
```xml 
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
				<stringType>String</stringType>
			</configuration>
		</execution>
	</executions>
</plugin>
```

d. Next the configure our topics in our application.properties files as below:    
```xml 
spring.cloud.stream.function.definition=processAggregateRecords
spring.cloud.stream.bindings.processAggregateRecords-in-0.destination=employees-topic

spring.cloud.stream.kafka.streams.binder.brokers=localhost:9092
spring.cloud.stream.kafka.streams.binder.configuration.schema.registry.url=http://localhost:8081
spring.cloud.stream.kafka.streams.binder.configuration.commit.interval.ms=10000
spring.cloud.stream.kafka.streams.binder.configuration.state.dir=application-state-store
spring.cloud.stream.kafka.streams.binder.configuration.default.key.serde=org.apache.kafka.common.serialization.Serdes$StringSerde
spring.cloud.stream.kafka.streams.binder.configuration.default.value.serde=io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde
```

e. Next in our configuration package we put all our business logic under the function called processAggregateRecords(). Check the comments of this code for more explaination.    

f. Finally we start our zookeeper, kafka and confluent schema registry.     

g. Next we create the topic, and create a kafka avro console producer (special type in conflent kafka) using the below commands:     
```xml
bin/schema-registry-start etc/schema-registry/schema-registry.properties

bin/kafka-avro-console-producer --broker-list localhost:9092 --topic employees-topic \
--property value.schema='{"namespace": "guru.learningjournal.examples.kafka.model","type": "record","name": "Employee","fields": [{"name": "id","type": "string"},{"name": "name","type": "string"},{"name": "department","type": "string"},{"name": "salary","type":"int"}]}'
```

h. Next we can produce some sample avro formatted messages as below:    
```xml
{"id": "101", "name": "Prashant", "department": "engineering", "salary": 5000}
{"id": "102", "name": "John", "department": "accounts", "salary": 8000}
{"id": "103", "name": "Abdul", "department": "engineering", "salary": 3000}
{"id": "104", "name": "Melinda", "department": "support", "salary": 7000}
{"id": "105", "name": "Jimmy", "department": "support", "salary": 6000}
```
i. Next we start our consumer and check if the averages are computed correctly.     

j. But we have one problem with this approach. Since KStreams is a continous stream of new data(insert only), average will not be computed correctly if we insert some changes to our original data sent like below:    
```xml
{"id": "101", "name": "Prashant", "department": "support", "salary": 5000}
{"id": "104", "name": "Melinda", "department": "engineering", "salary": 7000}
```
k. The solution to this problem is KTable and in such use cases where the originally sent data needs to be changed we need to use a KTable which preserves the key and updates the record if the same key comes back once again.   

### 12) Calculating the average from a KTable of data (Project: ktable-aggregate-consumer)  - Input format: avro & Output format: avro - Using KTables's aggregate function.     

a. We had a problem with our earlier example, if the same record comes over again, the average is not computed correctly, so this solution is using KTable's aggregate to solve this problem     

b. Everything remains the same as our last project execpt for the implementation function in our configuration KafkaConsumer class's processAggregateRecords() method. Check the comments of this code for more explaination.    

c. Run the example using the same steps and data as the last problem and check the output of the computation.   



### 13) Count the total records in a given time frame (Project: windowing-aggregate-consumer)  - Input format: json - Using KStream's windowedBy() function.     

a. The dependencies needed are stright forward as per the pom.xml file and the application.properties are also the same, except for one new parameter     
```xml
spring.cloud.stream.kafka.streams.bindings.processWindowAggregateRecords-in-0.consumer.timestamp-extractor-bean-name=invoiceTimesExtractor
```

b. This will call a custome invoiceTimeExtractor class which implements the TimestampExtractor. The extract method will check the recevied invoice to see if it has the createdTimestamp and if now it will set the previous timestamp.   

c. Next check the implementation function in our configuration KafkaConsumer class's processWindowAggregateRecords() method. Check the comments of this code for more explaination.     

d. Start the server, create the input topic and produce some sample data as follows:     
```xml
STR1534:{"InvoiceNumber": 101,"CreatedTime": "1549360860000","StoreID": "STR1534", "TotalAmount": 1920}
STR1535:{"InvoiceNumber": 102,"CreatedTime": "1549360900000","StoreID": "STR1535", "TotalAmount": 1860}
STR1534:{"InvoiceNumber": 103,"CreatedTime": "1549360999000","StoreID": "STR1534", "TotalAmount": 2400}

STR1536:{"InvoiceNumber": 104,"CreatedTime": "1549361160000","StoreID": "STR1536", "TotalAmount": 8936}
STR1534:{"InvoiceNumber": 105,"CreatedTime": "1549361270000","StoreID": "STR1534", "TotalAmount": 6375}
STR1536:{"InvoiceNumber": 106,"CreatedTime": "1549361370000","StoreID": "STR1536", "TotalAmount": 9365}
```

e. Start the application and check the output after running the 1st 3 sample data and then run the next set of 3 sample data to check the output.    








