package com.zzy.dt.context;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TransactionTodo {
    private String transactionId;
    private String messageId;
    private String from;
    private Integer type;
}
