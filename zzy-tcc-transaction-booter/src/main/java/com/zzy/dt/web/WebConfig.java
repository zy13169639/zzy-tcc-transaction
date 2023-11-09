package com.zzy.dt.web;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @author Zhiyang.Zhang
 * @version 1.0
 * @date 2023/10/5 11:44
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Bean
    public FilterRegistrationBean filterRegistrationBean() {
        FilterRegistrationBean filterRegistrationBean = new FilterRegistrationBean();
        filterRegistrationBean.setFilter(new RequestBodyCacheFilter());
        filterRegistrationBean.setOrder(-200);
        filterRegistrationBean.addUrlPatterns("/*");
        return filterRegistrationBean;
    }


//    @Override
//    public void addInterceptors(InterceptorRegistry registry) {
//        registry.addInterceptor(new DtHandlerInterceptor()).addPathPatterns("/*");
//    }


}
