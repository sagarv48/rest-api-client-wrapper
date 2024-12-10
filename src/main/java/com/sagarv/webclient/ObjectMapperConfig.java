package com.sagarv.webclient;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ObjectMapperConfig {

    @Bean
    public ObjectMapper objectMapper() {
        // Create a new ObjectMapper instance
        ObjectMapper mapper = new ObjectMapper();

        // Register the JavaTimeModule to handle Java 8 date/time types
        mapper.registerModule(new JavaTimeModule());

        // Enable or disable various features
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        // Add any custom serializers/deserializers if needed
        // Example: mapper.registerModule(new MyCustomModule());

        // Configure other ObjectMapper features as required
        // Example: mapper.enable(SerializationFeature.INDENT_OUTPUT);

        return mapper;
    }
}
