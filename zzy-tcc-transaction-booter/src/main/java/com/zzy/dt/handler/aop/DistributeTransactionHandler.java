package com.zzy.dt.handler.aop;

import cn.hutool.core.util.ReflectUtil;
import com.zzy.dt.annotation.DistributeTransaction;
import com.zzy.dt.common.Constants;
import com.zzy.dt.common.exception.DtCommonException;
import com.zzy.dt.common.holder.PrimitiveTypeMappingHolder;
import com.zzy.dt.common.provider.UserProvider;
import com.zzy.dt.common.type.PrimitiveTypeMapping;
import com.zzy.dt.common.util.JsonUtils;
import com.zzy.dt.common.util.StringUtil;
import com.zzy.dt.context.*;
import com.zzy.dt.handler.AbstractRequestHandler;
import com.zzy.dt.transaction.extractor.TransactionInfoExtractor;
import com.zzy.dt.transaction.manager.TransactionManager;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Zhiyang.Zhang
 * @version 1.0
 * @date 2023/10/4 12:10
 */
@Aspect
@Slf4j
public class DistributeTransactionHandler extends AbstractRequestHandler implements Ordered {

    @Autowired(required = false)
    UserProvider userProvider;

    @Autowired
    TransactionManager transactionManager;

    @Autowired
    TransactionInfoExtractor transactionInfoExtractor;

    @Around("@annotation(distributeTransaction)")
    public Object aroundHandler(ProceedingJoinPoint joinPoint, DistributeTransaction distributeTransaction) throws Throwable {
        TransactionContext transactionContext = DtContextHolder.getTransactionContext();
        try {
            // 识别一次执行中多个方法上有@DistributeTransaction注解
            transactionContext.touch();
            Signature signature = joinPoint.getSignature();
            if (!(signature instanceof MethodSignature)) {
                throw new DtCommonException(Constants.SYSTEM_ERROR, "@DistributeTransaction注解必须标注方法.");
            }
            if (TransactionSynchronizationManager.isActualTransactionActive()) {
                throw new DtCommonException(Constants.SYSTEM_ERROR, "分布式事务起点不应在本地事务中");
            }
            transactionManager.registerOperationInfo(getOperationInfo(joinPoint, distributeTransaction));
            if (transactionContext.isTransactionEnter()) {
                // 事务起点
                TransactionInfo extract = transactionInfoExtractor.extract();
                populateMetaData(transactionContext, extract);
                transactionManager.begin();
            } else {
                transactionManager.refreshTransactionOperationInfo();
            }
            Object proceed = joinPoint.proceed();
            try {
                // 业务已经正常完成
                if (transactionContext.isRoot()) {
                    transactionManager.commit();
                }
            } catch (Throwable t) {
                log.error("发送事务确认消息失败", t);
            }
            return proceed;
        } catch (Throwable t) {
            if (transactionContext.isRoot()) {
                log.debug("主事务业务执行失败", t);
                try {
                    transactionManager.rollback();
                } catch (Throwable st) {
                    log.error("发送事务消息失败", st);
                }
            }
            throw t;
        } finally {
            DtContextHolder.getTransactionContext().release();
            if (transactionContext.getReferCount() == 0) {
                if (log.isDebugEnabled()) {
                    log.debug("[{}]移除事务上下文", DtContextHolder.getTransactionContext().getDtRootId());
                }
                DtContextHolder.removeTransactionContext();
            }
        }
    }

    private TransactionOperatorInfo getOperationInfo(ProceedingJoinPoint joinPoint, DistributeTransaction distributeTransaction) {
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        Method method = methodSignature.getMethod();
        Object target = joinPoint.getTarget();
        String className = target.getClass().getName();
        String commitMethodName = distributeTransaction.commitMethod();
        String rollbackMethodName = distributeTransaction.rollbackMethod();
        // 参数值
        Object[] args = joinPoint.getArgs();
        List<String> paramValues = new ArrayList<>();
        for (Object arg : args) {
            paramValues.add(JsonUtils.toStr(arg));
        }
        // 方法参数类型
        Class<?>[] parameterTypes = method.getParameterTypes();
        List<String> parameterTypeNames = Arrays.stream(parameterTypes).map(parameterType -> PrimitiveTypeMappingHolder.getWrapperClass(parameterType).getName()).collect(Collectors.toList());
        Method commitMethod = ReflectUtil.getMethod(target.getClass(), commitMethodName, parameterTypes);
        if (commitMethod == null) {
            throw new DtCommonException(Constants.SYSTEM_ERROR, "%s中未找到参数类型为%s的%s方法", className, StringUtil.toString(parameterTypeNames), commitMethodName);
        }
        Method rollbackMethod = ReflectUtil.getMethod(target.getClass(), rollbackMethodName, parameterTypes);
        if (rollbackMethod == null) {
            throw new DtCommonException(Constants.SYSTEM_ERROR, "%s中未找到参数类型为%s的%s方法", className, StringUtil.toString(parameterTypeNames), rollbackMethodName);
        }
        TransactionConfirmInfo confirmInfo = TransactionConfirmInfo.builder().args(args).target(joinPoint.getThis()).method(commitMethod).build();
        return TransactionOperatorInfo.builder().commitMethodName(commitMethodName).rollbackMethodName(rollbackMethodName)
                .className(className).paramType(parameterTypeNames).paramValues(paramValues).transactionConfirmInfo(confirmInfo)
                .build();
    }

    private void populateMetaData(TransactionContext transactionContext, TransactionInfo transactionInfo) {
        transactionContext.setDtRootId(transactionInfo.getRootId());
        transactionContext.setRoot(transactionInfo.isRoot());
        transactionContext.setFrom(transactionInfo.getFrom());
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 1000;
    }

}
