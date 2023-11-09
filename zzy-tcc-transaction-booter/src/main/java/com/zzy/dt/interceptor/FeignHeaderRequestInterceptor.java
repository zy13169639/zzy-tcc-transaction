package com.zzy.dt.interceptor;

import com.zzy.dt.common.Constants;
import com.zzy.dt.context.DtContextHolder;
import feign.RequestInterceptor;
import feign.RequestTemplate;

/**
 * feign请求拦截器
 *
 * @author Zhiyang.Zhang
 * @version 1.0
 * @date 2023/10/19 23:08
 */
public class FeignHeaderRequestInterceptor implements RequestInterceptor {

    @Override
    public void apply(RequestTemplate template) {
        if (DtContextHolder.getTransactionContext().isInDtTransaction()) {
            template.header(Constants.DT_ROOT_HEADER_NAME, DtContextHolder.getTransactionContext().getDtRootId());
            template.header(Constants.DT_FROM, DtContextHolder.getTransactionContext().getFrom());
        }
    }
}
