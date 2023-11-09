package com.zzy.dt.domain;

import com.zzy.dt.enums.GlobalTransactionStatus;
import com.zzy.dt.enums.TransactionStatus;
import com.zzy.dt.enums.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class TransactionLog {
    private Long id;
    private String rootId;
    private String branchId;
    private TransactionType type;
    private TransactionStatus status;
    private long busCompleteTime;
    // 存放事务的confirm和cancel方法元数据
    private String meta;
    // 事务发起者
    private String origin;
    // 全局事务状态
    private GlobalTransactionStatus globalStatus;
    // 当前应用名
    private String application;

    private Long createTime;
}
