package com.zzy.dt.job;

import cn.hutool.core.collection.CollectionUtil;
import com.zzy.dt.MqConstants;
import com.zzy.dt.common.enums.ExceptionEnum;
import com.zzy.dt.common.exception.DtCommonException;
import com.zzy.dt.communication.CommunicationConstants;
import com.zzy.dt.communication.RedisCommunicationMessage;
import com.zzy.dt.communication.RedisCommunicationSender;
import com.zzy.dt.domain.TransactionLog;
import com.zzy.dt.enums.GlobalTransactionStatus;
import com.zzy.dt.enums.RecoveryStatus;
import com.zzy.dt.enums.TransactionType;
import com.zzy.dt.jdbc.RecoveryLogExecutor;
import com.zzy.dt.mq.MqManager;
import com.zzy.dt.mq.TransactionMessage;
import com.zzy.dt.transaction.repository.TransactionRepository;
import com.zzy.jdbc.holder.ResourceManager;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class RecoveryTaskRunner {

    TransactionRepository repository;

    RedisCommunicationSender messageSender;

    MqManager mqManager;

    private RecoveryLogExecutor recoveryLogExecutor = new RecoveryLogExecutor();

    public RecoveryTaskRunner(MqManager mqManager, TransactionRepository repository, RedisCommunicationSender messageSender) {
        this.repository = repository;
        this.messageSender = messageSender;
        this.mqManager = mqManager;
    }

    public void run() throws Exception {
        List<TransactionLog> pendingBranchTransactionLog = repository.getPendingBranchTransactionLog();
        if (CollectionUtil.isNotEmpty(pendingBranchTransactionLog)) {
            Map<String, List<TransactionLog>> pendingLogMap = pendingBranchTransactionLog.stream().collect(Collectors.groupingBy(log -> log.getOrigin()));
            pendingLogMap.forEach((k, v) -> {
                // 当一个应用中有多个rootId一样的分支事务，那么肯定是这样调用的  A->B->A (解释调用链路上自己远程调了自己)
                Set<String> transactionIds = v.stream().map(TransactionLog::getRootId).collect(Collectors.toSet());
                if (CollectionUtil.isNotEmpty(transactionIds)) {
                    RedisCommunicationMessage redisCommunicationMessage = new RedisCommunicationMessage();
                    redisCommunicationMessage.setTransactionIds(transactionIds);
                    redisCommunicationMessage.setFrom(ResourceManager.getApplication());
                    redisCommunicationMessage.setTo(k);
                    redisCommunicationMessage.setType(CommunicationConstants.ASK_GLOBAL_TRANSACTION_STATUS);
                    try {
                        messageSender.send(redisCommunicationMessage);
                        if (log.isDebugEnabled()) {
                            log.debug("发送消息成功[{}]", redisCommunicationMessage);
                        }
                    } catch (Exception e) {
                        log.error("发送消息失败[{}]", redisCommunicationMessage, e);
                    }
                }
            });
        }
        List<TransactionLog> pendingRootTransactionLogList = repository.getPendingRootTransactionLog();
        if (CollectionUtil.isNotEmpty(pendingRootTransactionLogList)) {
            pendingRootTransactionLogList.forEach(pendingRootTransactionLog -> {
                GlobalTransactionStatus globalStatus = pendingRootTransactionLog.getGlobalStatus();
                if (globalStatus != GlobalTransactionStatus.CREATED) {
                    return;
                }
                try {
                    boolean executed = repository.executeInLocalTransaction(() -> {
                        try {
                            recoveryLogExecutor.insertLog(pendingRootTransactionLog.getRootId(), RecoveryStatus.TIMEOUT);
                            boolean updated = repository.updateGlobalStatus(pendingRootTransactionLog.getRootId(),
                                    ResourceManager.getApplication(), TransactionType.ROOT, GlobalTransactionStatus.CANCEL,
                                    pendingRootTransactionLog.getStatus());
                            if (!updated) {
                                throw DtCommonException.throwException(ExceptionEnum.CONCURRENCY);
                            }
                            return true;
                        } catch (Exception e) {
                            throw new DtCommonException(ExceptionEnum.SYSTEM_ERROR.getCode(), ExceptionEnum.SYSTEM_ERROR.getMessage(), e);
                        }
                    });
                    if (executed) {
                        TransactionMessage transactionMessage = mqManager.getMessage(MqConstants.DT_TRANSACTION_TOPIC, pendingRootTransactionLog.getRootId(),
                                String.valueOf(pendingRootTransactionLog.getId()), GlobalTransactionStatus.CANCEL.getCode());
                        this.mqManager.send(transactionMessage);
                    }
                } catch (Exception e) {
                    log.error("执行根事务补偿失败[{}]-[{}]", ResourceManager.getApplication(), pendingRootTransactionLog.getRootId(), e);
                }
            });
        }
    }
}
