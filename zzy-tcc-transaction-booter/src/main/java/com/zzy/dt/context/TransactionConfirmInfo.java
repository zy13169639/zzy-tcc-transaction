package com.zzy.dt.context;

import lombok.*;

import java.lang.reflect.Method;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionConfirmInfo {
    private Method method;
    private Object[] args;
    private Object target;

    @SneakyThrows
    public void invoke(){
        method.invoke(target,args);
    }

}
