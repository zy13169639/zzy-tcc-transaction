package com.zzy.dt.communication;


import cn.iocoder.yudao.framework.mq.core.RedisMQTemplate;

public class RedisCommunicationSender {

    private RedisMQTemplate redisMQTemplate;

    public RedisCommunicationSender(RedisMQTemplate redisMQTemplate) {
        this.redisMQTemplate = redisMQTemplate;
    }

    public void send(RedisCommunicationMessage message) throws Exception {
        redisMQTemplate.send(message);
    }

}
