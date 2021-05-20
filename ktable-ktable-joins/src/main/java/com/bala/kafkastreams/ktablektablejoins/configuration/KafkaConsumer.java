package com.bala.kafkastreams.ktablektablejoins.configuration;

import java.util.function.BiConsumer;

import org.apache.kafka.streams.kstream.KTable;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.bala.kafkastreams.ktablektablejoins.model.UserDetails;
import com.bala.kafkastreams.ktablektablejoins.model.UserLogin;

import lombok.extern.log4j.Log4j2;

import java.time.Instant;
import java.time.ZoneOffset;


@Log4j2
@Configuration
public class KafkaConsumer {
	@Bean
    public  BiConsumer<KTable<String, UserDetails>, KTable<String, UserLogin>>  process() {
		
		return (users, logins) -> {
			users.toStream().foreach((k, v) -> log.info("User Key: {}, Last Login: {}, Value{}",
	                k, Instant.ofEpochMilli(v.getLastLogin()).atOffset(ZoneOffset.UTC), v));

	        logins.toStream().foreach((k, v) -> log.info("Login Key: {}, Last Login: {}, Value{}",
	                k, Instant.ofEpochMilli(v.getCreatedTime()).atOffset(ZoneOffset.UTC), v));

	        logins.join(users, (l, u) -> {
	            u.setLastLogin(l.getCreatedTime());
	            return u;
	        }).toStream().foreach((k, v) -> log.info("Updated Last Login Key: {}, Last Login: {}", k,
	                Instant.ofEpochMilli(v.getLastLogin()).atOffset(ZoneOffset.UTC)));
			
		};
		
	}
    
}
