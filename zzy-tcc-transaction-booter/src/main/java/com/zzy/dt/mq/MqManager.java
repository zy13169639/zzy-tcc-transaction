package com.zzy.dt.mq;

/**
 * mq管理器
 *
 * @author Zhiyang.Zhang
 * @version 1.0
 * @date 2023/10/22 10:14
 */
public class MqManager {

    private MessageProvider messageProvider;

    private MessageConsumer messageConsumer;

    public MqManager(MessageProvider messageProvider) {
        this.messageProvider = messageProvider;
    }

    public TransactionMessage getMessage(String topic, String transactionId, String messageId, Integer type) {
        return this.messageProvider.of(topic, transactionId, messageId, type);
    }

    public void send(TransactionMessage message) {
        this.messageProvider.send(message);
    }

}
