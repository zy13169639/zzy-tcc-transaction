package com.zzy.dt.mq;

import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.zzy.dt.MqConstants;
import com.zzy.dt.common.job.ScheduledJob;
import com.zzy.dt.common.util.RedisUtils;
import com.zzy.dt.context.DtContextHolder;
import com.zzy.dt.context.TransactionOperatorContext;
import com.zzy.dt.context.TransactionTodo;
import com.zzy.dt.event.EventPublisher;
import com.zzy.dt.event.TransactionTodoEvent;
import com.zzy.dt.transaction.manager.TransactionManager;
import com.zzy.jdbc.holder.ResourceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 * 消息消费者
 *
 * @author Zhiyang.Zhang
 * @version 1.0
 * @date 2023/10/22 11:48
 */
public interface MessageConsumer<T extends TransactionMessage> {

    Logger LOGGER = LoggerFactory.getLogger(MessageConsumer.class);

    default TransactionManager getTransactionManager() {
        return SpringUtil.getApplicationContext().getBean(TransactionManager.class);
    }

    default void consumer(T message) {
        String redisKey = null;
        try {
            LOGGER.debug("接收到消息[{}]", message);
            String transactionId = message.getTransactionId();
            Integer type = message.getType();
            String from = message.getFrom();
            if (StrUtil.isEmpty(transactionId)) {
                LOGGER.error("事务id为空");
                return;
            }
            if (type != 0 && type != 1) {
                LOGGER.error("事务状态无效[{}]", type);
                return;
            }
            String finalRedisKey = MqConstants.DT_TRANSACTION_PENDING + ResourceManager.getApplication() + ":" + message.getMqMessageId();
            redisKey = finalRedisKey;
            Boolean success = RedisUtils.setIfAbsent(finalRedisKey, String.valueOf(System.currentTimeMillis()), 10L, TimeUnit.SECONDS);
            if (!success) {
                LOGGER.debug("消息队列中[{}]存在并发操作", finalRedisKey);
                return;
            }
            ScheduledJob.scheduleRenewalTask(finalRedisKey, () -> RedisUtils.expire(finalRedisKey, 10L, TimeUnit.SECONDS), 5L, TimeUnit.SECONDS);
            TransactionTodo transactionTodo = TransactionTodo.builder().messageId(message.getMessageId())
                    .from(from)
                    .type(type)
                    .transactionId(transactionId)
                    .build();
            EventPublisher.publish(new TransactionTodoEvent(transactionTodo));
            this.ackMessage(message);
        } catch (Throwable t) {
            LOGGER.error("[{}]处理消息失败[{}]", ResourceManager.getApplication(), message, t);
        } finally {
            if (StrUtil.isNotEmpty(redisKey)) {
                ScheduledJob.cancel(redisKey);
                RedisUtils.del(redisKey);
            }
        }
    }

    void ackMessage(T message);

}
