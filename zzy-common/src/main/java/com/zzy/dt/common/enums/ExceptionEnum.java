package com.zzy.dt.common.enums;

import com.zzy.dt.common.Constants;
import lombok.Getter;

/**
 * @author Zhiyang.Zhang
 * @version 1.0
 * @date 2023/10/4 12:15
 */
public enum ExceptionEnum implements IEnum<Integer>{


    NOT_NULL(Constants.NOT_NULL_EXCEPTION_CODE, "%s can not be null"),
    ARG(Constants.ARG_EXCEPTION_CODE,"%s is not valid"),


    CONCURRENCY(Constants.CONCURRENCY_EXCEPTION_CODE,"resources is busy"),


    SQL(Constants.SQL_EXCEPTION_CODE,"sql exception"),
    DATA_ERROR(Constants.SQL_DATA_EXCEPTION_CODE,"result count error"),


    SYSTEM_ERROR(Constants.SYSTEM_ERROR,"system error");

    ;
    @Getter
    private Integer code;

    @Getter
    private String message;

    ExceptionEnum(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

}
