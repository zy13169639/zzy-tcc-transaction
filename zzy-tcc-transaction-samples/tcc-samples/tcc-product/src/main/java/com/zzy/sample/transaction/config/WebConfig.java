package com.zzy.sample.transaction.config;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.time.Duration;

@Configuration
public class WebConfig {

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        RestTemplateBuilder restTemplateBuilder = builder.setConnectTimeout(Duration.ofSeconds(10));
        restTemplateBuilder = restTemplateBuilder.setReadTimeout(Duration.ofSeconds(3));
        return restTemplateBuilder.build();
    }

}
