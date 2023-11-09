package com.zzy.dt.communication;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.zzy.jdbc.holder.ResourceManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;

@Slf4j
public abstract class BaseMessageProcessor implements MessageProcessor, InitializingBean {

    @Override
    public void afterPropertiesSet() throws Exception {
        MessageProcessorRegistry.register(this);
    }

    @Override
    public void process(RedisCommunicationMessage message) throws Exception {
        String to = message.getTo();
        if (StrUtil.isEmpty(to)) {
            log.warn("消息格式有误[{}]", message);
            return;
        }
        if (!to.equals(ResourceManager.getApplication())) {
            if (log.isDebugEnabled()) {
                log.debug("忽略消息[{}]-[{}]", ResourceManager.getApplication(), message);
            }
            return;
        }
        if (CollectionUtil.isEmpty(message.getTransactionIds())) {
            if (log.isDebugEnabled()) {
                log.debug("消息数据不完整[{}]", message);
            }
        }
        doProcess(message);
    }

    public abstract void doProcess(RedisCommunicationMessage message) throws Exception;
}
