package com.zzy.dt.enums;

import com.zzy.dt.common.enums.IEnum;
import lombok.Getter;

public enum TransactionStatus implements IEnum<Integer> {
    CREATED(1),
    BUSINESS_COMPLETE(2),
    // 目前后面两个没用到
    CONFIRMED(3),
    CANCELED(4),
    ;

    @Getter
    private Integer code;

    TransactionStatus(Integer code) {
        this.code = code;
    }

}
