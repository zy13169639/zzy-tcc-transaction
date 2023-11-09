package com.zzy.dt.jdbc;

import com.zzy.dt.domain.RecoveryLog;
import com.zzy.dt.enums.RecoveryStatus;
import com.zzy.jdbc.JdbcExecutor;
import com.zzy.jdbc.holder.ResourceManager;

import java.time.LocalDateTime;

public class RecoveryLogExecutor extends JdbcExecutor<RecoveryLog> {

    public boolean insertLog(RecoveryLog recoveryLog) throws Exception {
        return this.insert(ResourceManager.getTransactionConnection(), recoveryLog);
    }

    public boolean insertLog(String logId, RecoveryStatus recoveryStatus) throws Exception {
        RecoveryLog recoveryLog = new RecoveryLog();
        recoveryLog.setLogId(logId);
        recoveryLog.setStatus(recoveryStatus);
        recoveryLog.setApplication(ResourceManager.getApplication());
        recoveryLog.setCreateTime(LocalDateTime.now());
        return this.insertLog(recoveryLog);
    }

}
