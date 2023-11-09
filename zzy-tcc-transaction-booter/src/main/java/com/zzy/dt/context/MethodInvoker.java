package com.zzy.dt.context;

import com.zzy.dt.transaction.repository.MethodInvokerCallback;
import lombok.*;

import java.lang.reflect.Method;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MethodInvoker {
    private Method confirmMethod;
    private Method cancelMethod;
    private Method invokeMethod;
    private Object object;
    private Object[] args;
    private MethodInvokerCallback callback;
    private TransactionOperatorInfo operatorInfo;

    @SneakyThrows
    public void invoke() {
        callback.invokeCallBack(operatorInfo);
        invokeMethod.invoke(object, args);
    }
}
