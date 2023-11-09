package com.zzy.dt.mq;

/**
 * 消息发送方
 *
 * @author Zhiyang.Zhang
 * @version 1.0
 * @date 2023/10/22 10:13
 */

public interface MessageProvider<T extends TransactionMessage> {

    void send(T message);

    TransactionMessage of(String topic, String transactionId, String messageId, Integer type);

}
