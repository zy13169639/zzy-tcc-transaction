package com.zzy.dt.web;

import cn.hutool.core.util.ArrayUtil;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.ByteArrayInputStream;
import java.io.IOException;

/**
 * @author Zhiyang.Zhang
 * @version 1.0
 * @date 2023/10/4 12:29
 */
@Slf4j
public class RequestBodyCachedRequest extends HttpServletRequestWrapper {

    private byte[] body;

    public RequestBodyCachedRequest(HttpServletRequest request) throws IOException {
        super(request);
        int contentLength = request.getContentLength();
        if (contentLength > 0) {
            this.body = new byte[contentLength];
            try {
                request.getInputStream().read(body, 0, contentLength);
            } catch (IOException e) {
                log.error("解析请求流出错", e);
            }
        }
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
        return new CachedServletInputStream(body);
    }

    public String getBodyString() {
        if (ArrayUtil.isEmpty(body)) {
            return "";
        }
        return new String(body);
    }
}
