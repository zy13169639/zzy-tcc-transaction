package com.zzy.dt.mq;

import cn.iocoder.yudao.framework.mq.core.RedisMQTemplate;
import cn.iocoder.yudao.framework.mq.core.stream.AbstractTransactionStreamMessageListener;
import com.zzy.jdbc.holder.ResourceManager;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RedisTransactionMessageOperator extends AbstractTransactionStreamMessageListener<RedisTransactionMessage> implements MessageOperator<RedisTransactionMessage> {

    private RedisMQTemplate mqTemplate;

    public RedisTransactionMessageOperator(RedisMQTemplate mqTemplate) {
        this.mqTemplate = mqTemplate;
    }

    @Override
    public void onMessage(RedisTransactionMessage message) {
        this.consumer(message);
    }

    @Override
    public void ackMessage(RedisTransactionMessage message) {
        mqTemplate.getRedisTemplate().opsForStream().acknowledge(message.getTopic(), ResourceManager.getApplication(), message.getMqMessageId());
    }

    @Override
    public void send(RedisTransactionMessage message) {
        log.debug("发送消息[{}]", message);
        this.mqTemplate.send(message);
    }

    @Override
    public TransactionMessage of(String topic, String transactionId, String messageId, Integer type) {
        RedisTransactionMessage transactionMessage = new RedisTransactionMessage();
        transactionMessage.setTransactionId(transactionId);
        transactionMessage.setMessageId(messageId);
        transactionMessage.setType(type);
        transactionMessage.setTopic(topic);
        return transactionMessage;
    }

}
