package com.zzy.dt.interceptor;

import com.zzy.dt.common.Constants;
import com.zzy.dt.context.DtContextHolder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;

/**
 * RestTemplate请求头拦截器
 *
 * @author Zhiyang.Zhang
 * @version 1.0
 * @date 2023/10/19 23:16
 */
public class HeaderClientHttpRequestInterceptor implements ClientHttpRequestInterceptor {

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        if (DtContextHolder.getTransactionContext().isInDtTransaction()) {
            HttpHeaders headers = request.getHeaders();
            headers.add(Constants.DT_ROOT_HEADER_NAME, DtContextHolder.getTransactionContext().getDtRootId());
            headers.add(Constants.DT_FROM, DtContextHolder.getTransactionContext().getFrom());
        }
        return execution.execute(request, body);
    }
}
