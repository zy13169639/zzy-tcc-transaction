package com.zzy.dt.domain;

import com.zzy.dt.enums.RecoveryStatus;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class RecoveryLog {
    private String logId;
    private String application;
    private LocalDateTime createTime;
    private RecoveryStatus status;
}
