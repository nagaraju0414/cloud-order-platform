package com.cloudorder.order.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JacksonConfig {

    @Bean
    public ObjectMapper legacyObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();

        // Highly Recommended: Register the time module so Java 8 Instants/Dates 
        // serialize cleanly into your Outbox payloads without crashing
        //mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        return mapper;
    }
}
