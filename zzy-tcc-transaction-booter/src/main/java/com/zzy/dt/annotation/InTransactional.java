package com.zzy.dt.annotation;

import com.zzy.dt.enums.InTransactionalWorker;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * 该注解标识在带有@Transactional注解的方法上
 *
 * @author Zhiyang.Zhang
 * @version 1.0
 * @date 2023/10/17 16:21
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface InTransactional {

    /**
     * 指定处理器
     *
     * @return: com.zzy.dt.enums.InTransactionalEnum
     */
    InTransactionalWorker[] value();

}
