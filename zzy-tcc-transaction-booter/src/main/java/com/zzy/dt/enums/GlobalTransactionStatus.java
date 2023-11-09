package com.zzy.dt.enums;

import com.zzy.dt.common.enums.IEnum;

public enum GlobalTransactionStatus implements IEnum<Integer> {

    CONFIRM(0),
    CANCEL(1),
    CREATED(-1),
    ;

    private Integer code;

    GlobalTransactionStatus(Integer code) {
        this.code = code;
    }

    @Override
    public Integer getCode() {
        return this.code;
    }
}
