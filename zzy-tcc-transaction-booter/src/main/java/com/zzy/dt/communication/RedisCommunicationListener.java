package com.zzy.dt.communication;

import cn.hutool.core.collection.CollectionUtil;
import cn.iocoder.yudao.framework.mq.core.stream.AbstractGeneralStreamMessageListener;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class RedisCommunicationListener extends AbstractGeneralStreamMessageListener<RedisCommunicationMessage> {

    @Override
    public void onMessage(RedisCommunicationMessage message) {
        List<MessageProcessor> processors = MessageProcessorRegistry.getHandlers(message.getType());
        if (CollectionUtil.isEmpty(processors)) {
            log.warn("[{}]无效的消息类型[{}]", message.getTransactionIds(), message.getType());
            return;
        }
        for (MessageProcessor processor : processors) {
            try {
                processor.process(message);
            } catch (Exception e) {
                log.error("处理消息失败[{}]-[{}]", processor.getClass(), message, e);
            }
        }
    }

}
