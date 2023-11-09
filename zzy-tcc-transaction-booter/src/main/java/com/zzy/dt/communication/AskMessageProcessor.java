package com.zzy.dt.communication;

import cn.hutool.core.collection.CollectionUtil;
import com.zzy.dt.domain.TransactionLog;
import com.zzy.dt.transaction.repository.TransactionRepository;
import com.zzy.jdbc.holder.ResourceManager;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

import java.io.Serializable;
import java.util.*;

@Slf4j
public class AskMessageProcessor extends BaseMessageProcessor {

    private static final String DB_QUERY_LOCK = "dt:transaction:query:";

    private RedissonClient redissonClient;

    private TransactionRepository repository;

    private RedisCommunicationSender messageSender;

    public AskMessageProcessor(TransactionRepository repository, RedissonClient redissonClient, RedisCommunicationSender messageSender) {
        this.repository = repository;
        this.redissonClient = redissonClient;
        this.messageSender = messageSender;
    }

    @Override
    public Integer getType() {
        return CommunicationConstants.ASK_GLOBAL_TRANSACTION_STATUS;
    }

    @Override
    public void doProcess(RedisCommunicationMessage message) throws Exception {
        String lockKey = DB_QUERY_LOCK + ResourceManager.getApplication();
        RLock lock = redissonClient.getLock(lockKey);
        if (lock.tryLock()) {
            try {
                RedisCommunicationMessage redisCommunicationMessage = new RedisCommunicationMessage();
                Map<String, Serializable> transactionTypeMap = new HashMap<>();
                // TODO 编写批量查询主事务接口
                if (CollectionUtil.isEmpty(message.getTransactionIds())) {
                    return;
                }
                for (String transactionId : message.getTransactionIds()) {
                    TransactionLog rootTransactionLog = repository.getGlobalCompleteRootTransactionLog(transactionId);
                    if (rootTransactionLog == null) {
                        log.warn("[{}]未查询到已完成的主事务信息[{}]", ResourceManager.getApplication(), transactionId);
                        continue;
                    }
                    transactionTypeMap.put(rootTransactionLog.getRootId(), rootTransactionLog.getGlobalStatus().getCode());
                }
                if (CollectionUtil.isNotEmpty(transactionTypeMap)) {
                    redisCommunicationMessage.setExtra(transactionTypeMap);
                    redisCommunicationMessage.setFrom(ResourceManager.getApplication());
                    redisCommunicationMessage.setTo(message.getFrom());
                    redisCommunicationMessage.setTransactionIds(message.getTransactionIds());
                    redisCommunicationMessage.setType(CommunicationConstants.REPLY);
                    this.messageSender.send(redisCommunicationMessage);
                }
            } finally {
                lock.unlock();
            }
        }
    }
}
