package com.zzy.dt.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * RestTemplate配置
 *
 * @author Zhiyang.Zhang
 * @version 1.0
 * @date 2023/10/19 23:27
 */
@Configuration
public class RestTemplateConfig {

    @Bean
    @ConditionalOnBean(RestTemplate.class)
    public RestTemplateCustomize restTemplateCustomize(RestTemplate restTemplate) {
        return new RestTemplateCustomize(restTemplate);
    }

}
