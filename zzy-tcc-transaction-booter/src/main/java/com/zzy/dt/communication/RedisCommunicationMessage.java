package com.zzy.dt.communication;

import cn.iocoder.yudao.framework.mq.core.stream.AbstractStreamMessage;
import lombok.Data;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

/**
 * redis通信
 *
 * @author Zhiyang.Zhang
 * @version 1.0
 * @date 2023/10/23 16:48
 */

@Data
public class RedisCommunicationMessage extends AbstractStreamMessage {

    /**
     * 消息类型， 0：查询事务状态，1：返回的结果
     */
    private Integer type;

    /**
     * 事务id
     */
    private Set<String> transactionIds;

    /**
     * 哪个应用发起
     */
    private String from;

    /**
     * 发送给哪个用户
     */
    private String to;

    /**
     * 额外信息
     */
    private Map<String, Serializable> extra;

    public static final String COMMUNICATION_CHANNEL = "dt:transaction:communication";

    @Override
    public String getStreamKey() {
        return COMMUNICATION_CHANNEL;
    }
}
