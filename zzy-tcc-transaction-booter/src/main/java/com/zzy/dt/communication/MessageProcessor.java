package com.zzy.dt.communication;

public interface MessageProcessor  {

    Integer getType();

    void process(RedisCommunicationMessage message) throws Exception;

}
