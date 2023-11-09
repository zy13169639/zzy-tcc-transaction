package com.zzy.dt.context;

public class DtContextHolder {

    private static final ThreadLocal<TransactionContext> TRANSACTION_CONTEXT_HOLDER = ThreadLocal.withInitial(() -> new TransactionContext());

    private static final ThreadLocal<RepeatableContext> REPEATABLE_CONTEXT_HOLDER = ThreadLocal.withInitial(() -> new RepeatableContext());

    private static final ThreadLocal<TransactionOperatorContext> TRANSACTION_OPERATOR_CONTEXT_HOLDER = ThreadLocal.withInitial(() -> new TransactionOperatorContext());

    public static TransactionContext getTransactionContext() {
        return TRANSACTION_CONTEXT_HOLDER.get();
    }

    public static void removeTransactionContext() {
        TRANSACTION_CONTEXT_HOLDER.remove();
    }

    public static RepeatableContext getRepeatableContext() {
        return REPEATABLE_CONTEXT_HOLDER.get();
    }

    public static void removeRepeatableContext() {
        REPEATABLE_CONTEXT_HOLDER.remove();
    }

    public static TransactionOperatorContext getTransactionOperationContext() {
        return TRANSACTION_OPERATOR_CONTEXT_HOLDER.get();
    }

    public static void removeTransactionOperationContext() {
        TRANSACTION_OPERATOR_CONTEXT_HOLDER.remove();
    }

}
