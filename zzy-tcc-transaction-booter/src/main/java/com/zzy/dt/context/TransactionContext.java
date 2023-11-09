package com.zzy.dt.context;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * 事务上下文
 *
 * @author Zhiyang.Zhang
 * @version 1.0
 * @date 2023/10/19 23:17
 */
@Data
@NoArgsConstructor
public class TransactionContext {

    /**
     * 全局事务id
     */
    private String dtRootId;

    /**
     * 分支事务id
     */
    private String dtBranchId;

    /**
     * 是否是事务发起者
     */
    private boolean isRoot;

    /**
     * 关联请求数
     */
    @Getter
    private int referCount;

    /**
     * 对应数据库事务表id
     */
    private Long dataId;

    /**
     * 事务发起者的applicationName
     */
    private String from;

    /**
     * 事务确认和取消的方法
     */
    private List<TransactionOperatorInfo> transactionOperatorInfos = new ArrayList<>();

    public void touch() {
        referCount++;
    }

    public void release() {
        referCount--;
    }

    /**
     * 是否是当前线程的事务入口
     *
     * @return: boolean
     */
    public boolean isTransactionEnter() {
        return referCount == 1;
    }

    /**
     * 是否在事务中
     *
     * @return: boolean
     */
    public boolean isInDtTransaction() {
        return referCount > 0;
    }


    public void addTransactionOperatorContext(TransactionOperatorInfo transactionOperatorInfo) {
        this.transactionOperatorInfos.add(transactionOperatorInfo);
    }
}
