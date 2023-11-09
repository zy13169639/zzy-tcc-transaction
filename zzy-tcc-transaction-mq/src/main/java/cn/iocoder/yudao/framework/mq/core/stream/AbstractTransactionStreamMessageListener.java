package cn.iocoder.yudao.framework.mq.core.stream;

import cn.hutool.core.util.TypeUtil;
import cn.iocoder.yudao.framework.mq.core.RedisMQTemplate;
import cn.iocoder.yudao.framework.mq.core.interceptor.RedisMessageInterceptor;
import cn.iocoder.yudao.framework.mq.core.message.AbstractRedisMessage;
import com.zzy.dt.common.util.JsonUtils;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.stream.ObjectRecord;
import org.springframework.data.redis.stream.StreamListener;

import java.lang.reflect.Type;
import java.util.List;

/**
 * 事务消息，需要手动确认
 *
 */
public abstract class AbstractTransactionStreamMessageListener<T extends AbstractStreamMessage>
        extends AbstractStreamMessageListener<T> {

}
