package com.lightspeed.task1.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import redis.clients.jedis.json.DefaultGsonObjectMapper;
import redis.clients.jedis.json.JsonObjectMapper;

@Configuration
public class AppConfig {
    @Bean
    public JsonObjectMapper jsonObjectMapper() {
        return new DefaultGsonObjectMapper();
    }
}
