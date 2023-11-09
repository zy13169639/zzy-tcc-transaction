package com.zzy.dt.communication;

import cn.hutool.core.lang.Assert;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MessageProcessorRegistry {

    private static final Map<Integer, List<MessageProcessor>> HANDLER_MAP = new HashMap<>();

    public static void register(MessageProcessor messageProcessor) {
        Integer type = messageProcessor.getType();
        Assert.isTrue(type != null, "类型不能为空");
        List<MessageProcessor> messageProcessors = HANDLER_MAP.get(type);
        if (messageProcessors == null) {
            messageProcessors = new ArrayList<>();
            HANDLER_MAP.put(type, messageProcessors);
        }
        messageProcessors.add(messageProcessor);
    }

    public static List<MessageProcessor> getHandlers(Integer type) {
        return HANDLER_MAP.get(type);
    }

}
