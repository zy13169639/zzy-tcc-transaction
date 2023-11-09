package com.zzy.dt.transaction.repository.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.zzy.dt.SqlConstants;
import com.zzy.dt.common.util.JsonUtils;
import com.zzy.dt.context.TransactionContext;
import com.zzy.dt.context.TransactionOperatorInfo;
import com.zzy.dt.domain.TransactionLog;
import com.zzy.dt.enums.GlobalTransactionStatus;
import com.zzy.dt.enums.TransactionStatus;
import com.zzy.dt.enums.TransactionType;
import com.zzy.dt.transaction.Invoker;
import com.zzy.dt.transaction.repository.TransactionRepository;
import com.zzy.jdbc.JdbcExecutor;
import com.zzy.jdbc.holder.ResourceManager;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.ArrayList;
import java.util.List;

/**
 * jdbc事务
 *
 * @author Zhiyang.Zhang
 * @version 1.0
 * @date 2023/10/22 11:56
 */

@Slf4j
public class JdbcTransactionRepository extends JdbcExecutor<TransactionLog> implements TransactionRepository {


    private TransactionTemplate transactionTemplate;

    public JdbcTransactionRepository(TransactionTemplate transactionTemplate) {
        this.transactionTemplate = transactionTemplate;
    }

    @SneakyThrows
    public boolean createBranch(TransactionContext transactionContext) {
        TransactionLog initialLog = getInitialLog(TransactionType.BRANCH, transactionContext);
        return this.insert(ResourceManager.getTransactionConnection(), initialLog);
    }

    @SneakyThrows
    public boolean createRoot(TransactionContext transactionContext) {
        TransactionLog initialLog = getInitialLog(TransactionType.ROOT, transactionContext);
        return this.insert(ResourceManager.getTransactionConnection(), initialLog);
    }


    private TransactionLog getInitialLog(TransactionType transactionType, TransactionContext transactionContext) {
        TransactionLog transactionLog = TransactionLog.builder()
                .id(transactionContext.getDataId())
                .rootId(transactionContext.getDtRootId())
                .branchId(transactionContext.getDtBranchId())
                .type(transactionType)
                .status(TransactionStatus.CREATED)
                .origin(transactionContext.getFrom())
                .globalStatus(GlobalTransactionStatus.CREATED)
                .application(ResourceManager.getApplication())
                .createTime(System.currentTimeMillis())
                .meta(JsonUtils.toStr(transactionContext.getTransactionOperatorInfos()))
                .build();
        if (log.isDebugEnabled()) {
            log.debug("[{}]>>>create[{}]", ResourceManager.getApplication(), transactionLog.toString());
        }
        return transactionLog;
    }

    @Override
    @SneakyThrows
    public boolean refreshTransactionOperationInfo(TransactionContext transactionContext) {
        return this.updateTransactionOperationInfo(transactionContext.getDataId(), ResourceManager.getApplication(), JsonUtils.toStr(transactionContext.getTransactionOperatorInfos()));
    }

    @Override
    public boolean executeInLocalTransaction(Invoker invoker) {
        return transactionTemplate.execute(status -> invoker.invoke());
    }

    @Override
    @SneakyThrows
    public List<TransactionOperatorInfo> getPendingCommitTransactionOperationInfo(String transactionId) {
        List<TransactionOperatorInfo> operatorInfos = new ArrayList<>();
        List<TransactionLog> transactionLogList = this.getCompletedBranchTransactionLog(transactionId, ResourceManager.getApplication(), GlobalTransactionStatus.CREATED);
        if (CollectionUtil.isEmpty(transactionLogList)) {
            log.debug("[{}] - [{}]没有正常完成的业务", ResourceManager.getApplication(), transactionId);
            return operatorInfos;
        }
        for (TransactionLog transactionLog : transactionLogList) {
            List<TransactionOperatorInfo> transactionOperatorInfos = JsonUtils.toList(transactionLog.getMeta(), TransactionOperatorInfo.class);
            if (CollectionUtil.isEmpty(transactionOperatorInfos)) {
                continue;
            }
            for (TransactionOperatorInfo transactionOperatorInfo : transactionOperatorInfos) {
                transactionOperatorInfo.setBusCompleteTime(transactionLog.getBusCompleteTime());
            }
            operatorInfos.addAll(transactionOperatorInfos);
        }
        return operatorInfos;
    }

    @Override
    @SneakyThrows
    public TransactionLog getGlobalCompleteRootTransactionLog(String rootId) {
        return this.queryOne(ResourceManager.getTransactionConnection(), SqlConstants.SELECT_ROOT_TRANSACTION_LOG, rootId,
                TransactionType.ROOT, ResourceManager.getApplication(), GlobalTransactionStatus.CREATED);
    }

    @Override
    @SneakyThrows
    public List<TransactionLog> getPendingBranchTransactionLog() {
        return this.queryList(ResourceManager.getTransactionConnection(), SqlConstants.SELECT_TRANSACTION_LOG,
                ResourceManager.getApplication(), TransactionType.BRANCH, TransactionStatus.BUSINESS_COMPLETE,
                GlobalTransactionStatus.CREATED, System.currentTimeMillis());
    }


    @Override
    @SneakyThrows
    public List<TransactionLog> getPendingRootTransactionLog() {
        return this.queryList(ResourceManager.getTransactionConnection(), SqlConstants.SELECT_TIMEOUT_ROOT_TRANSACTION_LOG,
                ResourceManager.getApplication(), TransactionType.ROOT, GlobalTransactionStatus.CREATED, System.currentTimeMillis());
    }


    @Override
    @SneakyThrows
    public List<TransactionLog> getCompletedBranchTransactionLog(String rootId, String application, GlobalTransactionStatus globalTransactionStatus) {
        return this.queryList(ResourceManager.getTransactionConnection(), SqlConstants.SELECT_COMPLETE_TRANSACTION_LOG,
                rootId, application, TransactionStatus.BUSINESS_COMPLETE, globalTransactionStatus, TransactionType.BRANCH);
    }

    @Override
    @SneakyThrows
    public boolean updateTransactionOperationInfo(Long transactionId, String application, String meta) {
        return this.executeUpdate(ResourceManager.getTransactionConnection(), SqlConstants.UPDATE_TRANSACTION_META, meta, transactionId, application);
    }

    @Override
    @SneakyThrows
    public boolean updateGlobalStatus(String rootId, String application, TransactionType transactionType, GlobalTransactionStatus globalTransactionStatus, TransactionStatus transactionStatus) {
        return this.executeUpdate(ResourceManager.getTransactionConnection(), SqlConstants.UPDATE_GLOBAL_TRANSACTION_STATUS,
                globalTransactionStatus, rootId, application, transactionType, GlobalTransactionStatus.CREATED, transactionStatus);
    }

    @Override
    @SneakyThrows
    public boolean branchConfirm(Long transactionId, String application, TransactionType type) {
        return this.executeUpdate(ResourceManager.getTransactionConnection(), SqlConstants.UPDATE_TRANSACTION_LOG_BUS_TIME_AND_STATUS,
                TransactionStatus.BUSINESS_COMPLETE, System.currentTimeMillis(), transactionId, type, application);
    }


    @Override
    @SneakyThrows
    public boolean rootConfirm(Long transactionId, String application, TransactionType type) {
        return this.executeUpdate(ResourceManager.getTransactionConnection(), SqlConstants.UPDATE_ROOT_TRANSACTION_LOG_BUS_TIME_AND_STATUS,
                TransactionStatus.BUSINESS_COMPLETE, System.currentTimeMillis(), GlobalTransactionStatus.CONFIRM, transactionId, type, application);
    }

}
