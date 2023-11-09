package com.zzy.dt.config;

import cn.iocoder.yudao.framework.mq.core.RedisMQTemplate;
import com.zzy.dt.communication.*;
import com.zzy.dt.transaction.repository.TransactionRepository;
import org.redisson.api.RedissonClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class MqCommunicationConfig {

    @Bean
    public RedisCommunicationListener redisCommunicationListener() {
        return new RedisCommunicationListener();
    }

    @Bean
    public RedisCommunicationSender redisCommunicationSender(RedisMQTemplate redisMQTemplate) {
        return new RedisCommunicationSender(redisMQTemplate);
    }

    @Bean
    public AskMessageProcessor askCommunicationHandler(TransactionRepository repository, RedissonClient redissonClient, RedisCommunicationSender sender) {
        return new AskMessageProcessor(repository, redissonClient, sender);
    }

    @Bean
    public ReplyMessageProcessor replyCommunicationHandler() {
        return new ReplyMessageProcessor();
    }

}
