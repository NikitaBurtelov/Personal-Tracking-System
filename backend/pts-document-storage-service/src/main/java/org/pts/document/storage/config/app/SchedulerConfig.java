package org.pts.document.storage.config.app;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.SimpleAsyncTaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadPoolExecutor;

@Configuration
@EnableScheduling
public class SchedulerConfig {

    @Bean
    public Semaphore uploadDocumentJobSemaphore() {
        return new Semaphore(2);
    }

    @Bean
    public Semaphore deleteDocumentJobSemaphore() {
        return new Semaphore(1);
    }

    @Bean
    public Semaphore getDocumentJobSemaphore() {
        return new Semaphore(5);
    }

    @Bean
    public Semaphore updateJobStatusSemaphore() {
        return new Semaphore(1);
    }

    @Bean
    public TaskScheduler taskScheduler() {
        SimpleAsyncTaskScheduler scheduler = new SimpleAsyncTaskScheduler();
        scheduler.setVirtualThreads(true);
        scheduler.setThreadNamePrefix("sched-");
        return scheduler;
    }

    @Bean
    public ThreadPoolTaskExecutor uploadDocumentExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(4);

        executor.setQueueCapacity(100);

        executor.setThreadNamePrefix("upload-");

        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());

        executor.initialize();
        return executor;
    }

    @Bean
    public ThreadPoolTaskExecutor getDocumentExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(8);

        executor.setQueueCapacity(50);

        executor.setThreadNamePrefix("get-");

        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.AbortPolicy());

        executor.initialize();
        return executor;
    }

    @Bean
    public Executor taskExecutor() {
        return Executors.newVirtualThreadPerTaskExecutor();
    }
}