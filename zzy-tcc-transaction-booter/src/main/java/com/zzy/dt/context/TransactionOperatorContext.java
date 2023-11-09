package com.zzy.dt.context;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class TransactionOperatorContext {
    private Integer type;
    private String transactionId;
    private String messageId;
    private String from;
    // 业务完成时间
    private long busCompleteTime;
}
