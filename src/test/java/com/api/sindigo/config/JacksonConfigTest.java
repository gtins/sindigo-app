package com.api.sindigo.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class JacksonConfigTest {

    private final JacksonConfig jacksonConfig = new JacksonConfig();

    @Test
    void objectMapperBeanReturnsNewObjectMapperInstance() {
        assertInstanceOf(ObjectMapper.class, jacksonConfig.objectMapper());
    }
}

