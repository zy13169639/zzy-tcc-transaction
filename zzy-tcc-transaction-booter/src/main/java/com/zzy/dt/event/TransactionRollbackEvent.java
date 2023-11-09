package com.zzy.dt.event;

import com.zzy.dt.context.TransactionOperatorContext;
import org.springframework.context.ApplicationEvent;

public class TransactionRollbackEvent extends ApplicationEvent {

    public TransactionRollbackEvent(TransactionOperatorContext operatorContext) {
        super(operatorContext);
    }

}
