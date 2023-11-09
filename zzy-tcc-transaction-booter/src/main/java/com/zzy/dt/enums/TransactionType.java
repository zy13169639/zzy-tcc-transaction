package com.zzy.dt.enums;

import com.zzy.dt.common.enums.ExceptionEnum;
import com.zzy.dt.common.enums.IEnum;
import com.zzy.dt.common.exception.DtCommonException;
import lombok.Getter;

import java.util.Arrays;

public enum TransactionType implements IEnum<Integer> {

    ROOT(1),
    BRANCH(2),
    ;
    @Getter
    private Integer code;

    TransactionType(Integer code) {
        this.code = code;
    }

}
