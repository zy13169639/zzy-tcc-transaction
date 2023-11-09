package com.zzy.dt.context;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class RepeatableContext {

    private String requestKey;

    private boolean idempotent;

    private String module;

}
