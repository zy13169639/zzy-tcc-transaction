package com.zzy.dt.event;

import cn.hutool.core.util.StrUtil;
import com.zzy.dt.MqConstants;
import com.zzy.dt.common.job.ScheduledJob;
import com.zzy.dt.common.util.RedisUtils;
import com.zzy.dt.context.DtContextHolder;
import com.zzy.dt.context.TransactionOperatorContext;
import com.zzy.dt.context.TransactionTodo;
import com.zzy.dt.enums.GlobalTransactionStatus;
import com.zzy.dt.transaction.manager.TransactionManager;
import com.zzy.jdbc.holder.ResourceManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;

import java.util.concurrent.TimeUnit;

@Slf4j
public class TransactionTodoListener implements ApplicationListener<TransactionTodoEvent> {

    private TransactionManager transactionManager;

    public TransactionTodoListener(TransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }

    @Override
    public void onApplicationEvent(TransactionTodoEvent event) {
        String executingKey = null;
        TransactionTodo todoTransaction = (TransactionTodo) event.getSource();
        try {
            String finalExecutingKey = MqConstants.DT_TRANSACTION_EXECUTING + ResourceManager.getApplication() + ":" + todoTransaction.getTransactionId();
            executingKey = finalExecutingKey;
            Boolean success = RedisUtils.setIfAbsent(finalExecutingKey, String.valueOf(System.currentTimeMillis()), 10L, TimeUnit.SECONDS);
            if (!success) {
                log.debug("[{}]业务存在并发操作", finalExecutingKey);
                return;
            }
            ScheduledJob.scheduleRenewalTask(finalExecutingKey, () -> RedisUtils.expire(finalExecutingKey, 10L, TimeUnit.SECONDS), 5L, TimeUnit.SECONDS);
            TransactionOperatorContext transactionOperationContext = DtContextHolder.getTransactionOperationContext();
            transactionOperationContext.setTransactionId(todoTransaction.getTransactionId());
            transactionOperationContext.setMessageId(todoTransaction.getMessageId());
            transactionOperationContext.setFrom(todoTransaction.getFrom());
            transactionOperationContext.setType(todoTransaction.getType());
            if (todoTransaction.getType() == GlobalTransactionStatus.CONFIRM.getCode()) {
                this.onMessageCommit(todoTransaction.getTransactionId());
            } else if (todoTransaction.getType() == GlobalTransactionStatus.CANCEL.getCode()) {
                this.onMessageRollback(todoTransaction.getTransactionId());
            } else {
                log.error("[{}]无效的事务类型[{}]", todoTransaction.getTransactionId(), todoTransaction.getType());
            }
        } finally {
            DtContextHolder.removeTransactionOperationContext();
            if (StrUtil.isNotEmpty(executingKey)) {
                ScheduledJob.cancel(executingKey);
                RedisUtils.del(executingKey);
            }
        }
    }

    private void onMessageCommit(String transactionId) {
        this.transactionManager.onMessageCommit(transactionId);
    }

    private void onMessageRollback(String transactionId) {
        this.transactionManager.onMessageRollback(transactionId);
    }

}
