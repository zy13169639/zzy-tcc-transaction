package com.zzy.dt.context;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class TransactionOperatorInfo {
    private String className;
    private String commitMethodName;
    private String rollbackMethodName;
    private List<String> paramType;
    private List<String> paramValues;
    private long busCompleteTime;
    @JsonIgnore
    private TransactionConfirmInfo transactionConfirmInfo;

}
