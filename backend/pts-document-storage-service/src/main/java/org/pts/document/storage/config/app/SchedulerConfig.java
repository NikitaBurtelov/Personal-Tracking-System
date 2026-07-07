package org.pts.document.storage.config.app;

import lombok.RequiredArgsConstructor;
import org.pts.document.storage.config.app.properties.DocumentStorageApplicationProperties;
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
@RequiredArgsConstructor
public class SchedulerConfig {
    private final DocumentStorageApplicationProperties properties;

    @Bean
    public Semaphore publicationEventProcessSemaphore() {
        return new Semaphore(properties.getPublicationEventProcessSemaphoreSettings().getPermits());
    }

    @Bean
    public Semaphore uploadDocumentProcessSemaphore() {
        return new Semaphore(properties.getUploadDocumentProcessSemaphoreSettings().getPermits());
    }

    @Bean
    public Semaphore deleteDocumentProcessSemaphore() {
        return new Semaphore(properties.getDeleteDocumentProcessSemaphoreSettings().getPermits());
    }

    @Bean
    public Semaphore getDocumentProcessSemaphore() {
        return new Semaphore(properties.getGetDocumentProcessSemaphoreSettings().getPermits());
    }

    @Bean
    public Semaphore updateJobStatusProcessSemaphore() {
        return new Semaphore(properties.getUpdateJobStatusProcessSemaphoreSettings().getPermits());
    }

    @Bean
    public TaskScheduler taskScheduler() {
        SimpleAsyncTaskScheduler scheduler = new SimpleAsyncTaskScheduler();
        scheduler.setVirtualThreads(true);
        scheduler.setThreadNamePrefix("sched-");
        return scheduler;
    }

    @Bean
    public ThreadPoolTaskExecutor uploadDocumentProcessExecutor() {
        var executorSettings = properties.getUploadDocumentProcessExecutorSettings();

        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        executor.setCorePoolSize(executorSettings.getCorePoolSize());
        executor.setMaxPoolSize(executorSettings.getMaxPoolSize());

        executor.setQueueCapacity(executorSettings.getQueueCapacity());

        executor.setThreadNamePrefix("upload-");

        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());

        executor.initialize();
        return executor;
    }

    @Bean
    public ThreadPoolTaskExecutor getDocumentProcessExecutor() {
        var executorSettings = properties.getGetDocumentProcessExecutorSettings();

        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        executor.setCorePoolSize(executorSettings.getCorePoolSize());
        executor.setMaxPoolSize(executorSettings.getMaxPoolSize());

        executor.setQueueCapacity(executorSettings.getQueueCapacity());

        executor.setThreadNamePrefix("get-");

        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.AbortPolicy());

        executor.initialize();
        return executor;
    }

    @Bean
    public Executor taskProcessExecutor() {
        return Executors.newVirtualThreadPerTaskExecutor();
    }
}