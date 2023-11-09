package com.zzy.jdbc.config;

import com.zzy.jdbc.holder.ResourceManager;
import com.zzy.jdbc.propeties.JdbcProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(JdbcProperties.class)
public class JdbcAutoConfig {

    @Bean
    public ResourceManager resourceManager() {
        return new ResourceManager();
    }


}
