package com.github.artlibs.testcase;

import org.springframework.scheduling.annotation.Scheduled;

/**
 * Spring Scheduled Case
 *
 * @author Fury
 * @since 2024-04-30
 * <p>
 * All rights Reserved.
 */
public class ScheduledCase implements Injected {
    @Scheduled(cron = "any cron")
    public void runScheduleTask() {
        graspInjected();
    }
}
