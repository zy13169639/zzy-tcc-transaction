package com.zzy.dt.enums;


import com.zzy.dt.transaction.worker.Worker;
import com.zzy.dt.transaction.worker.impl.BusinessSuccessWorker;
import com.zzy.dt.transaction.worker.impl.IdempotentWorker;
import lombok.Getter;

public enum InTransactionalWorker {

    BUSINESS_SUCCESS(new BusinessSuccessWorker()),
    IDEMPOTENT(new IdempotentWorker()),
    ;

    @Getter
    private Worker worker;

    InTransactionalWorker(Worker worker) {
        this.worker = worker;
    }
}
