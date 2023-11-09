package com.zzy.dt.event;

import cn.hutool.extra.spring.SpringUtil;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEvent;

/**
 * 事件发布
 *
 * @author Zhiyang.Zhang
 * @version 1.0
 * @date 2023/10/23 15:18
 */
public class EventPublisher {

    private static final ApplicationContext APPLICATION_CONTEXT;

    static {
        APPLICATION_CONTEXT = SpringUtil.getApplicationContext();
    }

    public static void publish(ApplicationEvent event) {
        APPLICATION_CONTEXT.publishEvent(event);
    }

}
