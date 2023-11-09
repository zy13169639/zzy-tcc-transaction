package com.zzy.dt.transaction.repository;

import com.zzy.dt.context.DtContextHolder;
import com.zzy.dt.context.MethodInvoker;
import com.zzy.dt.context.TransactionOperatorContext;
import com.zzy.dt.context.TransactionOperatorInfo;


/**
 * 用作解析时的额外处理
 *
 * @author Zhiyang.Zhang
 * @version 1.0
 * @date 2023/10/23 14:37
 */
public interface MethodInvokerCallback {

    void parseCallback(MethodInvoker methodInvoker);

    default void invokeCallBack(TransactionOperatorInfo transactionOperatorInfo) {
        TransactionOperatorContext transactionOperationContext = DtContextHolder.getTransactionOperationContext();
        transactionOperationContext.setBusCompleteTime(transactionOperatorInfo.getBusCompleteTime());
    }
}
