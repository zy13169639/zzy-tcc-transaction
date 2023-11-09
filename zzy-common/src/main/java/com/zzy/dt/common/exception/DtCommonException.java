package com.zzy.dt.common.exception;

import com.zzy.dt.common.enums.ExceptionEnum;
import lombok.Getter;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 异常类
 *
 * @author Zhiyang.Zhang
 * @version 1.0
 * @date 2023/10/4 12:14
 */
public class DtCommonException extends RuntimeException {

    @Getter
    private Integer code;

    public DtCommonException(Integer code, String message) {
        super(message);
        this.code = code;
    }

    public DtCommonException(Integer code, String message, String... args) {
        super(getMessage(message, args));
        this.code = code;
    }

    public DtCommonException(Integer code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

    public DtCommonException(Integer code, String message, Throwable cause, String... args) {
        super(getMessage(message, args), cause);
        this.code = code;
    }

    public static DtCommonException throwException(ExceptionEnum exceptionEnum, String... args) {
        return new DtCommonException(exceptionEnum.getCode(), getMessage(exceptionEnum, args));
    }

    public static DtCommonException throwException(ExceptionEnum exceptionEnum, Throwable cause, String... args) {
        return new DtCommonException(exceptionEnum.getCode(), getMessage(exceptionEnum, args), cause);
    }

    private static String getMessage(ExceptionEnum exceptionEnum, String... args) {
        return String.format(exceptionEnum.getMessage(), args);
    }

    private static String getMessage(String formattedMessage, String... args) {
        return String.format(formattedMessage, args);
    }

}
