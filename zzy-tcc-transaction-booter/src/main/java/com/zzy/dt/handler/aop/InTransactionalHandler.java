package com.zzy.dt.handler.aop;

import com.zzy.dt.annotation.InTransactional;
import com.zzy.dt.common.provider.UserProvider;
import com.zzy.dt.enums.InTransactionalWorker;
import com.zzy.dt.handler.AbstractRequestHandler;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.support.TransactionSynchronizationManager;


/**
 * 事务内执行
 *
 * @author Zhiyang.Zhang
 * @version 1.0
 * @date 2023/10/17 16:12
 */
@Aspect
public class InTransactionalHandler extends AbstractRequestHandler {

    @Autowired
    UserProvider userProvider;

    @Around("@annotation(inTransactional)")
    public Object aroundHandler(ProceedingJoinPoint joinPoint, InTransactional inTransactional) throws Throwable {
        if (inTransactional == null) {
            return joinPoint.proceed();
        }
        boolean actualTransactionActive = TransactionSynchronizationManager.isActualTransactionActive();
        if (!actualTransactionActive) {
            throw new IllegalStateException("@InTransactional注解必须标注在带有事务的方法上");
        }
        // 先执行业务，然后让业务完成时间大于业务的执行时间，让后续业务通过确认时间来判定这条数据是否被修改过
        Object proceed = joinPoint.proceed();
        InTransactionalWorker[] handlers = inTransactional.value();
        for (InTransactionalWorker handler : handlers) {
            handler.getWorker().doWork();
        }
        return proceed;
    }
}
