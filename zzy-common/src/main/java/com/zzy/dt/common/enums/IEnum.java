package com.zzy.dt.common.enums;

import com.zzy.dt.common.exception.DtCommonException;

import java.util.Arrays;

public interface IEnum<E> {
    E getCode();

    default IEnum of(E code) {
        boolean assignable = Enum.class.isAssignableFrom(this.getClass());
        if(!assignable){
            throw DtCommonException.throwException(ExceptionEnum.SYSTEM_ERROR);
        }
        return Arrays.stream(this.getClass().getEnumConstants()).filter(em -> String.valueOf(em.getCode()).equals(String.valueOf(code))).findFirst().orElse(null);
    }
}
