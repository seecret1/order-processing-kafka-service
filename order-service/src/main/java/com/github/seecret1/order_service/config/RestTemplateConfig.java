package com.github.seecret1.order_service.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

@EnableRetry
@EnableScheduling
@Configuration
public class RestTemplateConfig {

    @Value("${spring.web.client.rest.connect-timeout}")
    private Duration connectionTimeout;

    @Value("${spring.web.client.rest.read-timeout}")
    private Duration readTimeout;

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder
                .connectTimeout(connectionTimeout)
                .readTimeout(readTimeout)
                .build();
    }
}
