package com.zzy.dt.event;

import com.zzy.dt.context.TransactionTodo;
import org.springframework.context.ApplicationEvent;

public class TransactionTodoEvent extends ApplicationEvent {

    public TransactionTodoEvent(TransactionTodo transactionTodo) {
        super(transactionTodo);
    }
}
