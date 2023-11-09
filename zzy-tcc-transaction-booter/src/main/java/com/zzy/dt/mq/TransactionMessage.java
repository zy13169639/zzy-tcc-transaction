package com.zzy.dt.mq;

/**
 * 事务消息
 *
 * @author Zhiyang.Zhang
 * @version 1.0
 * @date 2023/10/22 10:10
 */
public interface TransactionMessage {

    /**
     * 获取根事务id
     *
     * @return: java.lang.String
     */
    String getTransactionId();

    /**
     * 事务类型
     *
     * @return: java.lang.Integer
     */
    Integer getType();

    /**
     * 事务起点
     *
     * @return: java.lang.String
     */
    String getFrom();

    /**
     * 消息队列的topic
     *
     * @return: java.lang.String
     */
    String getTopic();

    /**
     * 接收到的message在mq中的id
     *
     * @return: java.lang.String
     */
    String getMqMessageId();

    /**
     * 消息的唯一标识(用来幂等等操作)
     *
     * @return: java.lang.String
     */
    String getMessageId();

}
