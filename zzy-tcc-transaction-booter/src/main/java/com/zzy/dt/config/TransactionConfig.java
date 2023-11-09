package com.zzy.dt.config;

import cn.iocoder.yudao.framework.mq.core.RedisMQTemplate;
import cn.iocoder.yudao.framework.mq.core.stream.AbstractTransactionStreamMessageListener;
import com.zzy.dt.communication.RedisCommunicationSender;
import com.zzy.dt.event.TransactionTodoListener;
import com.zzy.dt.job.RecoveryTaskRunner;
import com.zzy.dt.job.RedisPendingMessageResendJob;
import com.zzy.dt.job.TransactionRecoveryJob;
import com.zzy.dt.mq.MessageConsumer;
import com.zzy.dt.mq.MessageProvider;
import com.zzy.dt.mq.MqManager;
import com.zzy.dt.mq.RedisTransactionMessageOperator;
import com.zzy.dt.transaction.extractor.TransactionInfoExtractor;
import com.zzy.dt.transaction.extractor.impl.SpringbootTransactionInfoExtractor;
import com.zzy.dt.transaction.manager.TransactionManager;
import com.zzy.dt.transaction.repository.TransactionRepository;
import com.zzy.dt.transaction.repository.impl.JdbcTransactionRepository;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;

@Configuration
public class TransactionConfig {

    @Bean("dtTransactionManager")
    public TransactionManager transactionManager(TransactionRepository executor, MqManager mqManager) {
        return new TransactionManager(executor, mqManager);
    }

    @Bean
    @ConditionalOnMissingBean(TransactionRepository.class)
    public TransactionRepository transactionExecutor(TransactionTemplate transactionTemplate) {
        return new JdbcTransactionRepository(transactionTemplate);
    }

    @Bean
    public TransactionRecoveryJob transactionRecoveryJob(RedissonClient redissonClient, RecoveryTaskRunner recoveryTaskRunner) {
        return new TransactionRecoveryJob(redissonClient, recoveryTaskRunner);
    }

    @Bean
    public RecoveryTaskRunner recoveryTaskRunner(MqManager mqManager, TransactionRepository repository, RedisCommunicationSender messageSender) {
        return new RecoveryTaskRunner(mqManager, repository, messageSender);
    }

    @Bean
    @ConditionalOnMissingBean({MessageProvider.class, MessageConsumer.class})
    public RedisTransactionMessageOperator dtTransactionListener(RedisMQTemplate redisMQTemplate) {
        return new RedisTransactionMessageOperator(redisMQTemplate);
    }

    @Bean
    public MqManager mqManager(MessageProvider sender) {
        return new MqManager(sender);
    }

    @Bean
    @ConditionalOnBean(RedisTransactionMessageOperator.class)
    public RedisPendingMessageResendJob redisPendingMessageResendJob(List<AbstractTransactionStreamMessageListener<?>> listeners,
                                                                     RedisMQTemplate redisTemplate,
                                                                     @Value("${spring.application.name}") String groupName,
                                                                     RedissonClient redissonClient) {
        return new RedisPendingMessageResendJob(listeners, redisTemplate, groupName, redissonClient);
    }

    @Bean
    @ConditionalOnMissingBean(TransactionInfoExtractor.class)
    public SpringbootTransactionInfoExtractor transactionInfoExtractor() {
        return new SpringbootTransactionInfoExtractor();
    }

    @Bean
    public TransactionTodoListener transactionTodoListener(TransactionManager transactionManager) {
        return new TransactionTodoListener(transactionManager);
    }

}
