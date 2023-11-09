package com.zzy.dt.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class IdempotentLog {
    private Long id;
    private String requestKey;
    // 对于微服务用同一个数据库的应用增加识别标识
    private String application;
    private String module;
    private String transactionId;
    private LocalDateTime createTime;
}
