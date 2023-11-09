package com.zzy.dt.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 避免用户重复提交与幂等控制注解
 * @author Zhiyang.Zhang
 * @date 2023/10/19 23:08
 * @version 1.0
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface RejectRepeatable {

    /**
     * 超时时间，默认10s
     *
     * @return: long 单位ms
     */
    long timeout() default 1 * 10 * 1000;

    /**
     例如：如果消费的kafka的消息，那么可以以topic+messageId的方式来保证唯一性
     *
     * @return: java.lang.String
     */
    String key() default "";

    /**
     * 是否包含url后面的请求参数
     *
     * @return: boolean
     */
    boolean containsQueryString() default true;

    /**
     * false：只能防止timeout时间内的重复提交<br></br>
     * true：支持幂等
     * @return: boolean
     */
    boolean idempotent() default false;

    /**
     模块
     *
     * @return: java.lang.String
     */

    String module();

}
