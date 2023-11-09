package com.zzy.dt.event;

import com.zzy.dt.context.TransactionOperatorContext;
import com.zzy.dt.jdbc.IdempotentLogExecutor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;

/**
 * 用来回滚幂等相关的操作
 *
 * @author Zhiyang.Zhang
 * @version 1.0
 * @date 2023/10/23 15:18
 */
@Slf4j
public class IdempotentTransactionRollbackListener implements ApplicationListener<TransactionRollbackEvent> {

    private IdempotentLogExecutor idempotentLogExecutor;

    public IdempotentTransactionRollbackListener(IdempotentLogExecutor idempotentLogExecutor) {
        this.idempotentLogExecutor = idempotentLogExecutor;
    }

    @Override
    @SneakyThrows
    public void onApplicationEvent(TransactionRollbackEvent event) {
        TransactionOperatorContext eventSource = (TransactionOperatorContext) event.getSource();
        String transactionId = eventSource.getTransactionId();
        idempotentLogExecutor.deleteByTransactionId(transactionId);
    }
}
