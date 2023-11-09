package com.zzy.dt.transaction.worker.impl;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.zzy.dt.common.enums.ExceptionEnum;
import com.zzy.dt.common.exception.DtCommonException;
import com.zzy.dt.context.DtContextHolder;
import com.zzy.dt.context.RepeatableContext;
import com.zzy.dt.domain.IdempotentLog;
import com.zzy.dt.jdbc.IdempotentLogExecutor;
import com.zzy.dt.transaction.worker.Worker;
import com.zzy.jdbc.holder.ResourceManager;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;


/**
 * 幂等控制器
 *
 * @author Zhiyang.Zhang
 * @version 1.0
 * @date 2023/10/18 11:41
 */
@Slf4j
public class IdempotentWorker implements Worker {

    private IdempotentLogExecutor executor;

    public IdempotentWorker() {
        this.executor = new IdempotentLogExecutor();
    }

    @Override
    @SneakyThrows
    public void doWork() {
        try {
            RepeatableContext repeatableContext = DtContextHolder.getRepeatableContext();
            if (!repeatableContext.isIdempotent()) {
                log.warn("未指定幂等操作");
                return;
            }
            if (StrUtil.isNotBlank(repeatableContext.getRequestKey())) {
                boolean exist = executor.existIdempotentLog(repeatableContext.getModule(), repeatableContext.getRequestKey());
                if (exist) {
                    throw DtCommonException.throwException(ExceptionEnum.CONCURRENCY);
                }
                IdempotentLog log = IdempotentLog.builder().id(IdUtil.getSnowflake().nextId())
                        .requestKey(repeatableContext.getRequestKey())
                        .application(ResourceManager.getApplication())
                        .module(repeatableContext.getModule())
                        .createTime(LocalDateTime.now())
                        .build();
                if (DtContextHolder.getTransactionContext().isInDtTransaction()) {
                    log.setTransactionId(DtContextHolder.getTransactionContext().getDtRootId());
                }
                executor.insertIdempotentLog(log);
            }
        } finally {
            if (!DtContextHolder.getTransactionContext().isInDtTransaction()) {
                DtContextHolder.removeTransactionContext();
            }
        }
    }
}
