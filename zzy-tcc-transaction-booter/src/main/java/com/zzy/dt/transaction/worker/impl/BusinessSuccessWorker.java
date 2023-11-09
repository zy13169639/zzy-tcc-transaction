package com.zzy.dt.transaction.worker.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.zzy.dt.common.Constants;
import com.zzy.dt.common.exception.DtCommonException;
import com.zzy.dt.context.DtContextHolder;
import com.zzy.dt.context.TransactionContext;
import com.zzy.dt.domain.RecoveryLog;
import com.zzy.dt.enums.GlobalTransactionStatus;
import com.zzy.dt.enums.RecoveryStatus;
import com.zzy.dt.enums.TransactionType;
import com.zzy.dt.jdbc.RecoveryLogExecutor;
import com.zzy.dt.transaction.repository.TransactionRepository;
import com.zzy.dt.transaction.worker.Worker;
import com.zzy.jdbc.holder.ResourceManager;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;

/**
 * 更改事务状态
 *
 * @author Zhiyang.Zhang
 * @version 1.0
 * @date 2023/10/18 11:45
 */

@Slf4j
public class BusinessSuccessWorker implements Worker {

    private TransactionRepository repository;

    private RecoveryLogExecutor recoveryLogExecutor = new RecoveryLogExecutor();

    @Override
    @SneakyThrows
    public void doWork() {
        TransactionContext transactionContext = DtContextHolder.getTransactionContext();
        if (StrUtil.isEmpty(transactionContext.getDtRootId())) {
            return;
        }
        if (transactionContext.isRoot()) {
            transactionContext.getTransactionOperatorInfos().forEach(op -> op.getTransactionConfirmInfo().invoke());
            recoveryLogExecutor.insertLog(transactionContext.getDtRootId(), RecoveryStatus.NORMAL);
            boolean success = getRepository().rootConfirm(transactionContext.getDataId(), ResourceManager.getApplication(), TransactionType.ROOT);
            if (!success) {
                throw new DtCommonException(Constants.SYSTEM_ERROR, "主事务确认失败[%s]", transactionContext.getDataId().toString());
            }
            if (log.isDebugEnabled()) {
                log.debug("主事务业务处理成功[{}]", transactionContext.getDtRootId());
            }
        } else {
            boolean success = getRepository().branchConfirm(transactionContext.getDataId(), ResourceManager.getApplication(), TransactionType.BRANCH);
            if (!success) {
                throw new DtCommonException(Constants.SYSTEM_ERROR, "分支事务确认失败[%s]", transactionContext.getDataId().toString());
            }
            if (log.isDebugEnabled()) {
                log.debug("分支事务业务处理成功[{}][{}]", transactionContext.getDtRootId(), transactionContext.getDtBranchId());
            }
        }
    }

    private TransactionRepository getRepository() {
        if (repository == null) {
            synchronized (BusinessSuccessWorker.class) {
                if (repository == null) {
                    this.repository = SpringUtil.getBean(TransactionRepository.class);
                }
            }
        }
        return this.repository;
    }

}
