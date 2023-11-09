package com.zzy.dt.mq;

import cn.iocoder.yudao.framework.mq.core.stream.AbstractStreamMessage;
import com.zzy.dt.MqConstants;
import com.zzy.jdbc.holder.ResourceManager;
import lombok.Data;

/**
 * 基于redis队列的事务消息
 *
 * @author Zhiyang.Zhang
 * @version 1.0
 * @date 2023/10/22 10:12
 */
@Data
public class RedisTransactionMessage extends AbstractStreamMessage implements TransactionMessage {

    private String transactionId;
    // 消息类型
    // 0: 提交消息，1: 回滚消息
    private Integer type;

    private String topic;

    private String messageId;

    private String from = ResourceManager.getApplication();

    @Override
    public String getStreamKey() {
        return MqConstants.DT_TRANSACTION_TOPIC;
    }


}
