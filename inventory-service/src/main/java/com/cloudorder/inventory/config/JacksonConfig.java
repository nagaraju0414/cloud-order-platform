package com.cloudorder.inventory.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JacksonConfig {

    @Bean
    public ObjectMapper legacyObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();

        // Highly Recommended: Register the time module so Java 8 Instants/Dates 
        // serialize cleanly into your Outbox payloads without crashing
        mapper.registerModule((new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule()));
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        return mapper;
    }
}
