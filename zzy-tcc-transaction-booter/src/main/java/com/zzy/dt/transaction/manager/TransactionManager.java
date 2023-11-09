package com.zzy.dt.transaction.manager;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.zzy.dt.MqConstants;
import com.zzy.dt.common.Constants;
import com.zzy.dt.common.enums.ExceptionEnum;
import com.zzy.dt.common.exception.DtCommonException;
import com.zzy.dt.common.util.JsonUtils;
import com.zzy.dt.context.DtContextHolder;
import com.zzy.dt.context.MethodInvoker;
import com.zzy.dt.context.TransactionContext;
import com.zzy.dt.context.TransactionOperatorInfo;
import com.zzy.dt.domain.TransactionLog;
import com.zzy.dt.enums.GlobalTransactionStatus;
import com.zzy.dt.enums.TransactionStatus;
import com.zzy.dt.enums.TransactionType;
import com.zzy.dt.event.EventPublisher;
import com.zzy.dt.event.TransactionRollbackEvent;
import com.zzy.dt.mq.RedisTransactionMessage;
import com.zzy.dt.mq.MqManager;
import com.zzy.dt.mq.TransactionMessage;
import com.zzy.dt.transaction.repository.MethodInvokerCallback;
import com.zzy.dt.transaction.repository.TransactionRepository;
import com.zzy.jdbc.holder.ResourceManager;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * 事务控制器
 *
 * @author Zhiyang.Zhang
 * @version 1.0
 * @date 2023/10/22 11:34
 */
@Slf4j
public class TransactionManager {

    private TransactionRepository repository;

    private MqManager mqManager;

    public TransactionManager(TransactionRepository repository, MqManager mqManager) {
        this.repository = repository;
        this.mqManager = mqManager;
    }

    /**
     * 开启事务
     *
     * @return: boolean
     */
    public boolean begin() {
        boolean created;
        TransactionContext transactionContext = DtContextHolder.getTransactionContext();
        transactionContext.setDataId(this.repository.getDataId());
        if (transactionContext.isRoot()) {
            transactionContext.setDtRootId(this.repository.getTransactionId());
            transactionContext.setDtBranchId("-1");
            boolean createdRoot = this.repository.createRoot(transactionContext);
            created = createdRoot;
        } else {
            transactionContext.setDtBranchId(this.repository.getTransactionId());
            created = this.repository.createBranch(transactionContext);
        }
        if (!created) {
            throw new DtCommonException(Constants.SYSTEM_ERROR, "创建事务失败");
        }
        if (log.isDebugEnabled()) {
            if (DtContextHolder.getTransactionContext().isRoot()) {
                log.debug(">>>[{}]创建主事务成功", transactionContext.getDtRootId());
            } else {
                log.debug(">>>[{}]创建分支事务成功", transactionContext.getDtRootId());
            }
        }
        return true;
    }

    /**
     * 注册事务的操作信息
     *
     * @param operatorInfo
     * @return: boolean
     */
    public boolean registerOperationInfo(TransactionOperatorInfo operatorInfo) {
        DtContextHolder.getTransactionContext().addTransactionOperatorContext(operatorInfo);
        return true;
    }

    /**
     * 刷新事务的操作信息
     *
     * @return: boolean
     */
    public boolean refreshTransactionOperationInfo() {
        TransactionContext transactionContext = DtContextHolder.getTransactionContext();
        boolean success = repository.refreshTransactionOperationInfo(transactionContext);
        if (!success) {
            throw new DtCommonException(Constants.SYSTEM_ERROR, "更新事务信息失败");
        }
        log.debug(">>>[{}]刷新事务操作信息成功", DtContextHolder.getTransactionContext().getDtRootId());
        return true;
    }

    /**
     * 提交事务
     *
     * @return: boolean
     */
    public boolean commit() {
        TransactionContext transactionContext = DtContextHolder.getTransactionContext();
        if (transactionContext.isRoot()) {
            TransactionMessage transactionMessage = mqManager.getMessage(MqConstants.DT_TRANSACTION_TOPIC, transactionContext.getDtRootId(),
                    String.valueOf(repository.getDataId()), GlobalTransactionStatus.CONFIRM.getCode());
            this.mqManager.send(transactionMessage);
            if (log.isDebugEnabled()) {
                log.debug(">>>[{}]发送事务确认消息成功", transactionContext.getDtRootId());
            }
        }
        return true;
    }

    /**
     * 回滚事务
     *
     * @return: boolean
     */
    public boolean rollback() {
        TransactionContext transactionContext = DtContextHolder.getTransactionContext();
        if (transactionContext.isRoot()) {
            try {
                boolean status = repository.updateGlobalStatus(transactionContext.getDtRootId(), ResourceManager.getApplication(), TransactionType.ROOT, GlobalTransactionStatus.CANCEL, TransactionStatus.CREATED);
                if (!status) {
                    log.error("更新主事务失败[{}]", transactionContext.getDtRootId());
                }
            } catch (Exception e) {
                log.error("更新事务状态失败[{}]", transactionContext.getDtRootId(), e);
            }
            TransactionMessage transactionMessage = mqManager.getMessage(MqConstants.DT_TRANSACTION_TOPIC, transactionContext.getDtRootId(),
                    String.valueOf(repository.getDataId()), GlobalTransactionStatus.CANCEL.getCode());
            this.mqManager.send(transactionMessage);
            log.debug(">>>[{}]发送事务回滚消息成功", transactionContext.getDtRootId());
        }
        return true;
    }


    public void onMessageCommit(String transactionId) {
        if (log.isDebugEnabled()) {
            log.debug("[{}]全局事务提交", transactionId);
        }
        this.onMessage(transactionId, GlobalTransactionStatus.CONFIRM, methodInvoker -> methodInvoker.setInvokeMethod(methodInvoker.getConfirmMethod()));
    }

    public void onMessageRollback(String transactionId) {
        if (log.isDebugEnabled()) {
            log.debug("[{}]全局事务回滚", transactionId);
        }
        this.onMessage(transactionId, GlobalTransactionStatus.CANCEL, methodInvoker -> methodInvoker.setInvokeMethod(methodInvoker.getCancelMethod()));
    }

    private void onMessage(String transactionId, GlobalTransactionStatus globalTransactionStatus, MethodInvokerCallback callback) {
        List<TransactionOperatorInfo> pendingCommitTransactionOperationInfo = repository.getPendingCommitTransactionOperationInfo(transactionId);
        List<MethodInvoker> methodInvokers = repository.parseInvoker(pendingCommitTransactionOperationInfo, callback);
        if (CollectionUtil.isNotEmpty(methodInvokers)) {
            boolean success = repository.executeInLocalTransaction(() -> {
                // 先执行业务防止死锁
                methodInvokers.forEach(methodInvoker -> methodInvoker.invoke());
                boolean updated = repository.updateGlobalStatus(transactionId, ResourceManager.getApplication(), TransactionType.BRANCH, globalTransactionStatus, TransactionStatus.BUSINESS_COMPLETE);
                if (!updated) {
                    log.warn("[{}]全局事务状态更新失败[{}]->[{}]", transactionId, GlobalTransactionStatus.CREATED, globalTransactionStatus);
                    throw DtCommonException.throwException(ExceptionEnum.CONCURRENCY);
                }
                if (globalTransactionStatus == GlobalTransactionStatus.CANCEL) {
                    // 同步删除幂等数据
                    EventPublisher.publish(new TransactionRollbackEvent(DtContextHolder.getTransactionOperationContext()));
                }
                return true;
            });
            if (!success) {
                log.error("[{}]-[{}]执行失败", ResourceManager.getApplication(), transactionId);
            }
        }
    }

}
