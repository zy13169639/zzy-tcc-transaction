package com.zzy.dt.config;

import com.zzy.dt.common.provider.UserProvider;
import com.zzy.dt.handler.aop.DistributeTransactionHandler;
import com.zzy.dt.handler.aop.InTransactionalHandler;
import com.zzy.dt.handler.aop.RejectRepeatableHandler;
import com.zzy.dt.jdbc.IdempotentLogExecutor;
import com.zzy.dt.web.WebConfig;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * 自动配置类
 *
 * @author Zhiyang.Zhang
 * @version 1.0
 * @date 2023/10/4 11:57
 */
@Configuration
@Import({WebConfig.class, FeignConfig.class, TransactionConfig.class, IdempotentConfig.class, MqCommunicationConfig.class, RestTemplateConfig.class})
@ConditionalOnProperty(prefix = "dt", name = "enable", havingValue = "true", matchIfMissing = true)
@ConditionalOnBean(RedisTemplate.class)
public class DtAutoConfiguration {

    @Bean
    public InTransactionalHandler changeStatusHandler() {
        return new InTransactionalHandler();
    }

    @Bean
    public DistributeTransactionHandler distributeTransactionHandler() {
        return new DistributeTransactionHandler();
    }

    @Bean
    public RejectRepeatableHandler idempotentHandler() {
        return new RejectRepeatableHandler();
    }

    @Bean
    @ConditionalOnMissingBean(UserProvider.class)
    public UserProvider userProvider() {
        return new UserProvider() {
            @Override
            public String userKey() {
                return "-1";
            }

            @Override
            public String userId() {
                return "-1";
            }

            @Override
            public String userName() {
                return "UNKNOWN";
            }
        };
    }
}
