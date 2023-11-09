package cn.iocoder.yudao.framework.mq.config;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.system.SystemUtil;
import cn.iocoder.yudao.framework.mq.core.RedisMQTemplate;
import cn.iocoder.yudao.framework.mq.core.interceptor.RedisMessageInterceptor;
import cn.iocoder.yudao.framework.mq.core.pubsub.AbstractChannelMessageListener;
import cn.iocoder.yudao.framework.mq.core.stream.AbstractGeneralStreamMessageListener;
import cn.iocoder.yudao.framework.mq.core.stream.AbstractTransactionStreamMessageListener;
import cn.iocoder.yudao.framework.redis.config.YudaoRedisAutoConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.RedisServerCommands;
import org.springframework.data.redis.connection.stream.Consumer;
import org.springframework.data.redis.connection.stream.ObjectRecord;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.stream.DefaultStreamMessageListenerContainerX;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.List;
import java.util.Properties;

/**
 * 消息队列配置类
 *
 * @author 芋道源码
 */
@Slf4j
@EnableScheduling // 启用定时任务，用于 RedisPendingMessageResendJob 重发消息
@AutoConfigureAfter(YudaoRedisAutoConfiguration.class)
public class YudaoMQAutoConfiguration {

    @Bean
    public RedisMQTemplate redisMQTemplate(StringRedisTemplate redisTemplate,
                                           List<RedisMessageInterceptor> interceptors) {
        RedisMQTemplate redisMQTemplate = new RedisMQTemplate(redisTemplate);
        // 添加拦截器
        interceptors.forEach(redisMQTemplate::addInterceptor);
        return redisMQTemplate;
    }

    // ========== 消费者相关 ==========

    /**
     * 创建 Redis Pub/Sub 广播消费的容器
     */
    @Bean(initMethod = "start", destroyMethod = "stop")
    public RedisMessageListenerContainer redisMessageListenerContainer(
            RedisMQTemplate redisMQTemplate, List<AbstractChannelMessageListener<?>> listeners) {
        // 创建 RedisMessageListenerContainer 对象
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        // 设置 RedisConnection 工厂。
        container.setConnectionFactory(redisMQTemplate.getRedisTemplate().getRequiredConnectionFactory());
        // 添加监听器
        listeners.forEach(listener -> {
            listener.setRedisMQTemplate(redisMQTemplate);
            container.addMessageListener(listener, new ChannelTopic(listener.getChannel()));
            log.info("[redisMessageListenerContainer][注册 Channel({}) 对应的监听器({})]",
                    listener.getChannel(), listener.getClass().getName());
        });
        return container;
    }

    /**
     * 创建 Redis Stream 集群消费的容器
     * <p>
     * Redis Stream 的 xreadgroup 命令：https://www.geek-book.com/src/docs/redis/redis/redis.io/commands/xreadgroup.html
     */
    @Bean(initMethod = "start", destroyMethod = "stop")
    public StreamMessageListenerContainer<String, ObjectRecord<String, String>> redisTransactionStreamMessageListenerContainer(
            RedisMQTemplate redisMQTemplate, List<AbstractTransactionStreamMessageListener<?>> listeners) {
        RedisTemplate<String, ?> redisTemplate = redisMQTemplate.getRedisTemplate();
        checkRedisVersion(redisTemplate);
        // 第一步，创建 StreamMessageListenerContainer 容器
        // 创建 options 配置
        StreamMessageListenerContainer.StreamMessageListenerContainerOptions<String, ObjectRecord<String, String>> containerOptions =
                StreamMessageListenerContainer.StreamMessageListenerContainerOptions.builder()
                        .batchSize(1) // 一次性最多拉取多少条消息,这里让每个消费者只有一条未确认的消息
                        .targetType(String.class) // 目标类型。统一使用 String，通过自己封装的 AbstractStreamMessageListener 去反序列化
                        .build();
        // 创建 container 对象
        StreamMessageListenerContainer<String, ObjectRecord<String, String>> container =
//                StreamMessageListenerContainer.create(redisTemplate.getRequiredConnectionFactory(), containerOptions);
                DefaultStreamMessageListenerContainerX.create(redisMQTemplate.getRedisTemplate().getRequiredConnectionFactory(), containerOptions);

        // 第二步，注册监听器，消费对应的 Stream 主题
        String consumerName = buildConsumerName();
        listeners.parallelStream().forEach(listener -> {
            log.info("[redisTransactionStreamMessageListenerContainer][开始注册 StreamKey({}) 对应的监听器({})]",
                    listener.getStreamKey(), listener.getClass().getName());
            // 创建 listener 对应的消费者分组
            try {
                redisTemplate.opsForStream().createGroup(listener.getStreamKey(), listener.getGroup());
            } catch (Exception ignore) {
            }
            // 设置 listener 对应的 redisTemplate
            listener.setRedisMQTemplate(redisMQTemplate);
            // 创建 Consumer 对象
            Consumer consumer = Consumer.from(listener.getGroup(), consumerName);
            // 设置 Consumer 消费进度，以最小消费进度为准
            // 指定offset为0-0也没反应
            StreamOffset<String> streamOffset = StreamOffset.create(listener.getStreamKey(), ReadOffset.lastConsumed());
            // 设置 Consumer 监听
            StreamMessageListenerContainer.StreamReadRequestBuilder<String> builder = StreamMessageListenerContainer.StreamReadRequest
                    .builder(streamOffset).consumer(consumer)
                    .autoAcknowledge(false) // 不自动 ack
                    .cancelOnError(throwable -> false); // 默认配置，发生异常就取消消费，显然不符合预期；因此，我们设置为 false
            container.register(builder.build(), listener);
            log.info("[redisTransactionStreamMessageListenerContainer][完成注册 StreamKey({}) 对应的监听器({})]",
                    listener.getStreamKey(), listener.getClass().getName());
        });
        return container;
    }


    @Bean(initMethod = "start", destroyMethod = "stop")
    public StreamMessageListenerContainer<String, ObjectRecord<String, String>> redisGeneralStreamMessageListenerContainer(
            RedisMQTemplate redisMQTemplate, List<AbstractGeneralStreamMessageListener<?>> listeners) {
        RedisTemplate<String, ?> redisTemplate = redisMQTemplate.getRedisTemplate();
        checkRedisVersion(redisTemplate);
        // 第一步，创建 StreamMessageListenerContainer 容器
        // 创建 options 配置
        StreamMessageListenerContainer.StreamMessageListenerContainerOptions<String, ObjectRecord<String, String>> containerOptions =
                StreamMessageListenerContainer.StreamMessageListenerContainerOptions.builder()
                        .batchSize(10)
                        .targetType(String.class)
                        .build();
        // 创建 container 对象
        StreamMessageListenerContainer<String, ObjectRecord<String, String>> container =
                DefaultStreamMessageListenerContainerX.create(redisMQTemplate.getRedisTemplate().getRequiredConnectionFactory(), containerOptions);

        // 第二步，注册监听器，消费对应的 Stream 主题
        String consumerName = buildConsumerName();
        listeners.parallelStream().forEach(listener -> {
            log.info("[redisGeneralStreamMessageListenerContainer][开始注册 StreamKey({}) 对应的监听器({})]",
                    listener.getStreamKey(), listener.getClass().getName());
            // 创建 listener 对应的消费者分组
            try {
                redisTemplate.opsForStream().createGroup(listener.getStreamKey(), listener.getGroup());
            } catch (Exception ignore) {
            }
            // 设置 listener 对应的 redisTemplate
            listener.setRedisMQTemplate(redisMQTemplate);
            // 创建 Consumer 对象
            Consumer consumer = Consumer.from(listener.getGroup(), consumerName);
            // 设置 Consumer 消费进度，以最小消费进度为准
            // 指定offset为0-0也没反应
            StreamOffset<String> streamOffset = StreamOffset.create(listener.getStreamKey(), ReadOffset.lastConsumed());
            // 设置 Consumer 监听
            StreamMessageListenerContainer.StreamReadRequestBuilder<String> builder = StreamMessageListenerContainer.StreamReadRequest
                    .builder(streamOffset).consumer(consumer)
                    .autoAcknowledge(true)
                    .cancelOnError(throwable -> false);
            container.register(builder.build(), listener);
            log.info("[redisGeneralStreamMessageListenerContainer][完成注册 StreamKey({}) 对应的监听器({})]",
                    listener.getStreamKey(), listener.getClass().getName());
        });
        return container;
    }

    /**
     * 构建消费者名字，使用本地 IP + 进程编号的方式。
     * 参考自 RocketMQ clientId 的实现
     *
     * @return 消费者名字
     */
    private static String buildConsumerName() {
        return String.format("%s@%d", SystemUtil.getHostInfo().getAddress(), SystemUtil.getCurrentPID());
    }

    /**
     * 校验 Redis 版本号，是否满足最低的版本号要求！
     */
    private static void checkRedisVersion(RedisTemplate<String, ?> redisTemplate) {
        // 获得 Redis 版本
        Properties info = redisTemplate.execute((RedisCallback<Properties>) RedisServerCommands::info);
        String version = MapUtil.getStr(info, "redis_version");
        // 校验最低版本必须大于等于 5.0.0
        int majorVersion = Integer.parseInt(StrUtil.subBefore(version, '.', false));
        if (majorVersion < 5) {
            throw new IllegalStateException(StrUtil.format("您当前的 Redis 版本为 {}，小于最低要求的 5.0.0 版本！", version));
        }
    }

}
