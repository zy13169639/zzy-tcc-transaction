package com.zzy.dt.jdbc;

import com.zzy.dt.SqlConstants;
import com.zzy.dt.domain.IdempotentLog;
import com.zzy.jdbc.JdbcExecutor;
import com.zzy.jdbc.holder.ResourceManager;

import java.util.List;

public class IdempotentLogExecutor extends JdbcExecutor<IdempotentLog> {

    public boolean insertIdempotentLog(IdempotentLog idempotentLog) throws Exception {
        return this.insert(ResourceManager.getTransactionConnection(), idempotentLog);
    }

    public boolean existIdempotentLog(String module, String requestKey) throws Exception {
        Integer count = this.queryOne(ResourceManager.getTransactionConnection(), SqlConstants.SELECT_IDEMPOTENT_LOG, Integer.class, module, requestKey);
        return count > 0;
    }

    public void deleteByTransactionId(String transactionId) throws Exception {
        this.executeUpdate(ResourceManager.getTransactionConnection(), SqlConstants.DELETE_IDEMPOTENT_BY_TRANSACTION, transactionId);
    }

}
