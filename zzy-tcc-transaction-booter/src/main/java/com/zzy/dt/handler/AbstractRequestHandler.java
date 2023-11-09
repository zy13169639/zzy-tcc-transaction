package com.zzy.dt.handler;


import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.MD5;
import com.zzy.dt.common.provider.UserProvider;
import com.zzy.dt.common.util.SpringUtil;
import com.zzy.dt.common.util.WebUtil;
import com.zzy.dt.web.RequestBodyCachedRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 获取本次请求的key
 *
 * @author Zhiyang.Zhang
 * @version 1.0
 * @date 2023/10/13 11:24
 */
public abstract class AbstractRequestHandler {

    protected String getRequestKey(ProceedingJoinPoint joinPoint, String key, UserProvider userProvider, boolean containsQueryString) {
        String requestKey;
        if (StrUtil.isNotEmpty(key)) {
            // 如果指定了key，按照spel表达式处理
            requestKey = getKey(key, (MethodSignature) joinPoint.getSignature(), joinPoint.getArgs());
        } else {
            // 如果没指定key，那么就以请求数据为请求唯一标识
            HttpServletRequest httpRequest = WebUtil.getHttpRequest();
            if (httpRequest == null) {
                return "";
            }
            // TODO 是否要加上query string？
            RequestBodyCachedRequest cachedRequest = (RequestBodyCachedRequest) httpRequest;
            // 获取用户信息
            String userKey = Optional.ofNullable(userProvider).map(user -> user.userKey()).orElse("");
            String bodyString = cachedRequest.getBodyString();
            if (containsQueryString) {
                String queryString = cachedRequest.getQueryString();
                if (StrUtil.isNotBlank(queryString)) {
                    String[] split = queryString.split("&");
                    bodyString += "|" + Arrays.stream(split).sorted().collect(Collectors.joining("|"));
                }
            }
            if (StrUtil.isEmpty(bodyString)) {
                bodyString = UUID.fastUUID().toString(true);
            }
            requestKey = userKey + "|" + bodyString + "|" + cachedRequest.getRequestURI();
        }
        return MD5.create().digestHex(requestKey);
    }


    private String getKey(String key, MethodSignature methodSignature, Object[] args) {
        Method method = methodSignature.getMethod();
        Object spelValue = SpringUtil.getSpelValue(method, key, args);
        if (ObjectUtil.isNotEmpty(spelValue)) {
            key = String.valueOf(spelValue);
        }
        return key;
    }

}
