package com.zzy.dt.job;

import com.zzy.jdbc.holder.ResourceManager;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.LocalDateTime;

@Slf4j
public class TransactionRecoveryJob {

    private static final String REDIS_LOCK = "dt:transaction:recovery:lock:";

    RecoveryTaskRunner recoveryTaskRunner;

    RedissonClient redissonClient;

    public TransactionRecoveryJob(RedissonClient redissonClient, RecoveryTaskRunner recoveryTaskRunner) {
        this.recoveryTaskRunner = recoveryTaskRunner;
        this.redissonClient = redissonClient;
    }

    @Scheduled(cron = "*/15 * * * * ?")
    public void recovery() {
        RLock lock = redissonClient.getLock(REDIS_LOCK + ResourceManager.getApplication());
        boolean locked = lock.tryLock();
        if (locked) {
            try {
                log.info("[{}]开始事务补偿[{}]",ResourceManager.getApplication(), LocalDateTime.now());
                recoveryTaskRunner.run();
            } catch (Exception e) {
                log.error("执行补偿任务失败", e);
            } finally {
                lock.unlock();
                log.info("[{}]结束事务补偿[{}]",ResourceManager.getApplication(), LocalDateTime.now());
            }
        }
    }
}
