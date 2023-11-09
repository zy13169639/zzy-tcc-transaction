package com.zzy.dt.communication;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.thread.NamedThreadFactory;
import com.zzy.dt.context.TransactionTodo;
import com.zzy.dt.event.EventPublisher;
import com.zzy.dt.event.TransactionTodoEvent;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Slf4j
public class ReplyMessageProcessor extends BaseMessageProcessor {

    private final ThreadPoolExecutor executor;

    public ReplyMessageProcessor() {
        executor = new ThreadPoolExecutor(1, 4, 10, TimeUnit.MINUTES, new ArrayBlockingQueue<>(1024 * 10), new NamedThreadFactory("reply-msg", false), (r, executor) -> {
            int size = executor.getQueue().size();
            log.warn("线程池队列达到最大值[{}]", size);
        });
    }

    @Override
    public Integer getType() {
        return CommunicationConstants.REPLY;
    }

    @Override
    public void doProcess(RedisCommunicationMessage message) throws Exception {
        Map<String, Serializable> extra = message.getExtra();
        if (CollectionUtil.isEmpty(extra)) {
            log.warn("接收到消息数据不完整[{}]", message);
            return;
        }
        for (String transactionId : extra.keySet()) {
            TransactionTodo transactionTodo = TransactionTodo.builder()
                    .from(message.getFrom())
                    .type((Integer) extra.get(transactionId))
                    .transactionId(transactionId)
                    .build();
            executor.execute(() -> {
                try {
                    EventPublisher.publish(new TransactionTodoEvent(transactionTodo));
                } catch (Exception e) {
                    log.error("处理恢复消息异常[{}]", message, e);
                }
            });
        }
    }
}
