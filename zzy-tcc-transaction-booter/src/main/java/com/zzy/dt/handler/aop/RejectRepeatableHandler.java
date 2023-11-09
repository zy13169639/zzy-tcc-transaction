package com.zzy.dt.handler.aop;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.zzy.dt.annotation.RejectRepeatable;
import com.zzy.dt.common.Constants;
import com.zzy.dt.common.enums.ExceptionEnum;
import com.zzy.dt.common.exception.DtCommonException;
import com.zzy.dt.common.job.ScheduledJob;
import com.zzy.dt.common.provider.UserProvider;
import com.zzy.dt.common.util.RedisUtils;
import com.zzy.dt.context.DtContextHolder;
import com.zzy.dt.handler.AbstractRequestHandler;
import com.zzy.jdbc.holder.ResourceManager;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;

import java.util.concurrent.TimeUnit;


/**
 * 防重复提交
 *
 * @author Zhiyang.Zhang
 * @version 1.0
 * @date 2023/10/17 16:12
 */
@Aspect
@Slf4j
public class RejectRepeatableHandler extends AbstractRequestHandler implements Ordered {

    @Autowired
    UserProvider userProvider;

    @Around("@annotation(rejectRepeatable)")
    public Object aroundHandler(ProceedingJoinPoint joinPoint, RejectRepeatable rejectRepeatable) throws Throwable {
        if (rejectRepeatable == null) {
            return joinPoint.proceed();
        }
        String requestKey = null;
        boolean idempotent = rejectRepeatable.idempotent();
        try {
            long timeout = rejectRepeatable.timeout();
            long minTimeout = timeout / 3;
            // 如果小于500ms，那么取一半
            if (minTimeout < 500) {
                minTimeout = timeout / 2;
            }
            String key = rejectRepeatable.key();
            Signature signature = joinPoint.getSignature();
            if (!(signature instanceof MethodSignature)) {
                return joinPoint.proceed();
            }
            requestKey = getRequestKey(joinPoint, key, userProvider, rejectRepeatable.containsQueryString());
            if (StrUtil.isEmpty(requestKey)) {
                throw new IllegalArgumentException("方法指定了幂等，而requestKey为空");
            }
            String redisKey = getRedisKey(requestKey);
            Boolean canExecuted = RedisUtils.setIfAbsent(redisKey, String.valueOf(DateUtil.current()), timeout, TimeUnit.MILLISECONDS);
            if (!canExecuted) {
                throw DtCommonException.throwException(ExceptionEnum.CONCURRENCY);
            }
            if (idempotent) {
                log.debug("当前请求[{}]支持幂等操作", requestKey);
                // 如果要支持幂等
                DtContextHolder.getRepeatableContext().setRequestKey(requestKey);
                DtContextHolder.getRepeatableContext().setIdempotent(true);
                DtContextHolder.getRepeatableContext().setModule(rejectRepeatable.module());
            }
            ScheduledJob.scheduleRenewalTask(requestKey, () -> RedisUtils.expire(redisKey, timeout, TimeUnit.MILLISECONDS), minTimeout, TimeUnit.MILLISECONDS);
            return joinPoint.proceed();
        } catch (Throwable t) {
            // 执行异常也不释放资源,防止用户点击一次成功，第二次失败导致过期时间内key被删除
            // deleteFromRedis(requestKey);
            throw t;
        } finally {
            if(log.isDebugEnabled()) {
                log.debug("移除请求[{}]上下文", requestKey);
            }
            DtContextHolder.removeRepeatableContext();
            ScheduledJob.cancel(requestKey);
            if (idempotent) {
                deleteFromRedis(requestKey);
            }
        }
    }

    private String getRedisKey(String requestKey) {
        return Constants.REDIS_REQUEST_KEY + ResourceManager.getApplication() + ":" + requestKey;
    }

    private void deleteFromRedis(String requestKey) {
        if (StrUtil.isNotEmpty(requestKey)) {
            String redisKey = getRedisKey(requestKey);
            RedisUtils.del(redisKey);
        }
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 100;
    }
}
