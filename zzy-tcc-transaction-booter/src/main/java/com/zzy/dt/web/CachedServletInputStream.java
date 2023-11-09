package com.zzy.dt.web;

import lombok.Getter;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * @author Zhiyang.Zhang
 * @version 1.0
 * @date 2023/10/4 12:31
 */
public class CachedServletInputStream extends ServletInputStream {

    @Getter
    private ByteArrayInputStream inputStream;

    public CachedServletInputStream(byte[] body) {
        if(body==null){
            body = new byte[0];
        }
        this.inputStream = new ByteArrayInputStream(body);
    }

    @Override
    public boolean isFinished() {
        return this.inputStream.available() == 0;
    }

    @Override
    public boolean isReady() {
        return this.inputStream.available() > 0;
    }

    @Override
    public void setReadListener(ReadListener listener) {

    }

    @Override
    public int read() throws IOException {
        return this.inputStream.read();
    }


}
