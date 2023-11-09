package com.zzy.dt.web;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 *
 * @author Zhiyang.Zhang
 * @date 2023/10/5 11:42
 * @version 1.0
 */
public class RequestBodyCacheFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        chain.doFilter(new RequestBodyCachedRequest((HttpServletRequest) request),response);
    }

}
