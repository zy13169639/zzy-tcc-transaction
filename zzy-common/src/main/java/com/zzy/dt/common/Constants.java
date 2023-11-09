package com.zzy.dt.common;

/**
 * 常量类
 *
 * @author Zhiyang.Zhang
 * @version 1.0
 * @date 2023/10/4 12:27
 */
public interface Constants {

    /****************************************
     ***************字符串常量*****************
     ****************************************/

    String DT_ROOT_HEADER_NAME = "dt-root";
    String DT_FROM = "dt-from";

    String REQUEST_KEY_HEADER_NAME = "request-key";

    String REDIS_REQUEST_KEY = "dt:repeatable:request:";


    /****************************************
     ***************错误状态码*****************
     ****************************************/

    Integer NOT_NULL_EXCEPTION_CODE = 100000;
    Integer ARG_EXCEPTION_CODE = 100001;

    Integer CONCURRENCY_EXCEPTION_CODE = 500000;

    Integer SQL_EXCEPTION_CODE = 888888;
    Integer SQL_DATA_EXCEPTION_CODE = 800001;

    Integer SYSTEM_ERROR = 999999;


}
