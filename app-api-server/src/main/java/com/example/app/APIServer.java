package com.example.app;

import com.example.app.graphql.APIController;
import com.example.app.util.APIIntegrationClient;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import java.util.concurrent.TimeUnit;

@SpringBootApplication
@EnableCaching
public class APIServer {

    private final Logger logger = LoggerFactory.getLogger(APIServer.class);

    @Autowired
    APIIntegrationClient apiIntegrationClient;

    public static void main(String[] args) {
        SpringApplication.run(APIServer.class, args);
    }

    //-- Spring Boot Cache Configuration
    //-- Expiry Setup for 10 Seconds
    @Bean
    public Caffeine caffeineConfig() {
        return Caffeine.newBuilder().expireAfterWrite(10, TimeUnit.SECONDS);
    }

    //-- Spring Boot Cache Configuration
    @Bean
    public CacheManager cacheManager(Caffeine caffeine) {
        CaffeineCacheManager caffeineCacheManager = new CaffeineCacheManager();
        caffeineCacheManager.setCaffeine(caffeine);
        return caffeineCacheManager;
    }



}