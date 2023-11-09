package com.zzy.dt.common.job;

import io.netty.util.HashedWheelTimer;
import io.netty.util.Timeout;
import io.netty.util.TimerTask;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * 定时任务
 *
 * @author Zhiyang.Zhang
 * @version 1.0
 * @date 2023/10/12 10:29
 */
@Slf4j
public class ScheduledJob {

    private static final HashedWheelTimer WHEEL_TIMER;

    private static final Map<String, Timeout> TASK_MAP = new ConcurrentHashMap<>();

    private static final Set<String> RUNNER_TASK = new HashSet<>();

    static {
        WHEEL_TIMER = new HashedWheelTimer(r -> new Thread(r, "dt-schedule"), 200, TimeUnit.MILLISECONDS);
    }

    public static Timeout newTask(TimerTask task, Long timeout, TimeUnit timeUnit) {
        return WHEEL_TIMER.newTimeout(task, timeout, timeUnit);
    }


    public static Timeout newTask(TimerTask task, Long timeout) {
        return newTask(task, timeout, TimeUnit.MILLISECONDS);
    }

    public static void scheduleRenewalTask(final String taskKey, final Runnable runnable, Long timeout, TimeUnit timeUnit) {
        synchronized (ScheduledJob.class) {
            if (RUNNER_TASK.contains(taskKey)) {
                log.warn("task={},任务已在运行中", taskKey);
                return;
            }
            RUNNER_TASK.add(taskKey);
        }

        TimerTask task = new TimerTask() {
            @Override
            public void run(Timeout to) throws Exception {
                runnable.run();
                synchronized (ScheduledJob.class) {
                    if (RUNNER_TASK.contains(taskKey)) {
                        Timeout newTimeout = WHEEL_TIMER.newTimeout(this, timeout, timeUnit);
                        TASK_MAP.put(taskKey, newTimeout);
                    }
                }
            }
        };
        Timeout newTimeout = WHEEL_TIMER.newTimeout(task, timeout, timeUnit);
        TASK_MAP.put(taskKey, newTimeout);
    }

    public static void cancel(String taskKey) {
        synchronized (ScheduledJob.class) {
            if (!RUNNER_TASK.contains(taskKey)) {
                return;
            }
            RUNNER_TASK.remove(taskKey);
            Timeout timeout = TASK_MAP.remove(taskKey);
            if (timeout == null) {
                log.error("task={},状态不正确", taskKey);
                return;
            }
            timeout.cancel();
        }
    }


    public static void scheduleRenewalTask(final String taskKey, final Runnable runnable, Long timeout) {
        scheduleRenewalTask(taskKey, runnable, timeout, TimeUnit.MILLISECONDS);
    }

}
