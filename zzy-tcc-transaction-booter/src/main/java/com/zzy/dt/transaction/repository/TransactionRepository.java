package com.zzy.dt.transaction.repository;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.zzy.dt.common.util.JsonUtils;
import com.zzy.dt.context.MethodInvoker;
import com.zzy.dt.context.TransactionContext;
import com.zzy.dt.context.TransactionOperatorInfo;
import com.zzy.dt.domain.TransactionLog;
import com.zzy.dt.enums.GlobalTransactionStatus;
import com.zzy.dt.enums.TransactionStatus;
import com.zzy.dt.enums.TransactionType;
import com.zzy.dt.transaction.IdGenerator;
import com.zzy.dt.transaction.Invoker;
import lombok.SneakyThrows;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * 事务相关的操作
 *
 * @author Zhiyang.Zhang
 * @version 1.0
 * @date 2023/10/22 10:20
 */
public interface TransactionRepository extends IdGenerator {

    /**
     * 创建主事务
     *
     * @param transactionContext
     * @return: boolean
     */
    boolean createRoot(TransactionContext transactionContext);

    /**
     * 创建分支事务
     *
     * @param transactionContext
     * @return: boolean
     */

    boolean createBranch(TransactionContext transactionContext);

    /**
     * 获取全局事务状态已完成的主事务
     *
     * @param rootId
     * @return: com.zzy.dt.domain.TransactionLog
     */
    TransactionLog getGlobalCompleteRootTransactionLog(String rootId);

    /**
     * 获取未完成的分支事务（且超时10s）
     *
     * @return: java.util.List<com.zzy.dt.domain.TransactionLog>
     */
    List<TransactionLog> getPendingBranchTransactionLog();

    /**
     * 获取未完成的主事务（且超时10s）
     *
     * @return: java.util.List<com.zzy.dt.domain.TransactionLog>
     */
    List<TransactionLog> getPendingRootTransactionLog();


    /**
     * 获取状态为BUSINESS_COMPLETE的分支事务
     *
     * @param rootId      根事务id
     * @param application 应用名
     * @return: java.util.List<com.zzy.dt.domain.TransactionLog>
     */
    List<TransactionLog> getCompletedBranchTransactionLog(String rootId, String application, GlobalTransactionStatus globalTransactionStatus);


    /**
     * 修改本地事务状态为BUSINESS_COMPLETE
     *
     * @param transactionId 全局事务id
     * @param application   应用名
     * @param type
     * @return: boolean
     */
    boolean branchConfirm(Long transactionId, String application, TransactionType type);


    boolean rootConfirm(Long transactionId, String application, TransactionType type);

    /**
     * 更新全局的事务状态
     *
     * @param rootId                  根事务id
     * @param application             应用名
     * @param globalTransactionStatus 待更新的全局状态
     * @return: boolean
     */
    boolean updateGlobalStatus(String rootId, String application, TransactionType transactionType, GlobalTransactionStatus globalTransactionStatus, TransactionStatus transactionStatus);


    /**
     * 更新事务操作信息
     *
     * @param transactionId 根事务id
     * @param application   应用名
     * @param meta          操作信息
     * @return: boolean
     */
    boolean updateTransactionOperationInfo(Long transactionId, String application, String meta);

    /**
     * 刷新事务操作信息
     *
     * @param transactionContext
     * @return: boolean
     */
    boolean refreshTransactionOperationInfo(TransactionContext transactionContext);

    /**
     * 在同一本地事务中执行
     *
     * @param invoker
     * @return: boolean
     */
    boolean executeInLocalTransaction(Invoker invoker);

    /**
     * 获取全局待提交事务的操作信息
     *
     * @return: java.util.List<com.zzy.dt.context.TransactionOperatorInfo>
     */
    List<TransactionOperatorInfo> getPendingCommitTransactionOperationInfo(String transactionId);

    /**
     * 生成事务id
     *
     * @return: java.lang.String
     */
    @Override
    default String getTransactionId() {
        // return UUID.fastUUID().toString(true);
        return String.valueOf(IdUtil.getSnowflake().nextId());
    }

    /**
     * 生成数据id
     *
     * @return: java.lang.Long
     */
    @Override
    default Long getDataId() {
        return IdUtil.getSnowflake().nextId();
    }


    @SneakyThrows
    default List<MethodInvoker> parseInvoker(List<TransactionOperatorInfo> transactionOperatorInfos, MethodInvokerCallback callback) {
        List<MethodInvoker> methodInvokers = new ArrayList<>();
        if (CollectionUtil.isEmpty(transactionOperatorInfos)) {
            return methodInvokers;
        }
        for (TransactionOperatorInfo transactionOperatorInfo : transactionOperatorInfos) {
            MethodInvoker invoker = new MethodInvoker();
            String className = transactionOperatorInfo.getClassName();
            List<String> paramType = transactionOperatorInfo.getParamType();
            List<String> paramValues = transactionOperatorInfo.getParamValues();
            Object[] args = new Object[paramValues.size()];
            Class[] paramTypeClasses = new Class[paramType.size()];
            for (int i = 0; i < paramType.size(); i++) {
                Class<?> aClass = Class.forName(paramType.get(i));
                paramTypeClasses[i] = aClass;
                args[i] = JsonUtils.toObj(paramValues.get(i), aClass);
            }
            String commitMethodName = transactionOperatorInfo.getCommitMethodName();
            String rollbackMethodName = transactionOperatorInfo.getRollbackMethodName();
            Class<?> aClass = Class.forName(className);
            Object bean = SpringUtil.getBean(aClass);
            Method confirmMethod = ReflectUtil.getMethod(aClass, commitMethodName, paramTypeClasses);
            Method cancelMethod = ReflectUtil.getMethod(aClass, rollbackMethodName, paramTypeClasses);
            invoker.setObject(bean);
            invoker.setCancelMethod(cancelMethod);
            invoker.setConfirmMethod(confirmMethod);
            invoker.setArgs(args);
            invoker.setCallback(callback);
            invoker.setOperatorInfo(transactionOperatorInfo);
            callback.parseCallback(invoker);
            methodInvokers.add(invoker);
        }
        return methodInvokers;
    }
}
