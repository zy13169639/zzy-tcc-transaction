package com.zzy.dt.enums;

import com.zzy.dt.common.enums.IEnum;
import lombok.Getter;

public enum RecoveryStatus implements IEnum<Integer> {

    NORMAL(0),
    TIMEOUT(1),
    ;

    @Getter
    private Integer code;

    RecoveryStatus(Integer code) {
        this.code = code;
    }

}
