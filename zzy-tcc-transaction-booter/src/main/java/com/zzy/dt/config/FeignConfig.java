package com.zzy.dt.config;

import com.zzy.dt.interceptor.FeignHeaderRequestInterceptor;
import feign.Feign;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnClass(Feign.Builder.class)
public class FeignConfig {

    @Bean
    public FeignHeaderRequestInterceptor headerRequestInterceptor(){
        return new FeignHeaderRequestInterceptor();
    }

}
