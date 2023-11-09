package com.zzy.dt.config;

import com.zzy.dt.event.IdempotentTransactionRollbackListener;
import com.zzy.dt.jdbc.IdempotentLogExecutor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class IdempotentConfig {

    @Bean
    public IdempotentTransactionRollbackListener idempotentTransactionRollbackListener(IdempotentLogExecutor executor){
        return new IdempotentTransactionRollbackListener(executor);
    }

    @Bean
    public IdempotentLogExecutor idempotentLogExecutor() {
        return new IdempotentLogExecutor();
    }

}
