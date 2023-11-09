package com.zzy.dt.common.provider;

/**
 * @author Zhiyang.Zhang
 * @version 1.0
 * @date 2023/10/4 11:55
 */
public interface UserProvider {

    /**
     * 获取用户识别标识
     *
     * @return: java.lang.String
     */
    String userKey();

    /**
     * 获取用户id
     *
     * @return: java.lang.String
     */
    String userId();


    /**
     * 获取用户名
     *
     * @return: java.lang.String
     */
    String userName();

}
