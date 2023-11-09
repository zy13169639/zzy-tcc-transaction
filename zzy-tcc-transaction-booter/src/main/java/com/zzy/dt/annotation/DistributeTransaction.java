package com.zzy.dt.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 作用在消费端
 *
 * @author Zhiyang.Zhang
 * @version 1.0
 * @date 2023/10/4 11:44
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface DistributeTransaction {

    /**
     * 事务确认方法
     *
     * @return: java.lang.String
     */
    String commitMethod();

    /**
     * 事务回滚方法
     *
     * @return: java.lang.String
     */
    String rollbackMethod();

    /**
     * 超时时间，默认30s
     *
     * @return: long 单位ms
     */
    long timeout() default 1 * 30 * 1000;

}
