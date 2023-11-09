package com.zzy.dt.config;

import com.zzy.dt.interceptor.HeaderClientHttpRequestInterceptor;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.web.client.RestTemplate;

/**
 *
 * @author Zhiyang.Zhang
 * @date 2023/10/19 23:31
 * @version 1.0
 */
public class RestTemplateCustomize implements InitializingBean {

    private RestTemplate restTemplate;

    public RestTemplateCustomize(RestTemplate restTemplate){
        this.restTemplate = restTemplate;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        restTemplate.getInterceptors().add(new HeaderClientHttpRequestInterceptor());
    }
}
