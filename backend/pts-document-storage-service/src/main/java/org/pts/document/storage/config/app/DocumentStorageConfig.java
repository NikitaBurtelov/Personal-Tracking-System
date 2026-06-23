package org.pts.document.storage.config.app;

import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.pts.document.storage.config.app.properties.DocumentStorageApplicationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

@Configuration
@RequiredArgsConstructor
public class DocumentStorageConfig {
    private final DocumentStorageApplicationProperties properties;

    @Bean
    public ExecutorService documentExecutorService() {
        var executorSettings = properties.getDocumentServiceExecutorSettings();
        return new ThreadPoolExecutor(
                executorSettings.getCorePoolSize(),
                executorSettings.getMaxPoolSize(),
                0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(executorSettings.getQueueCapacity()),
                new ThreadFactory() {
                    private final AtomicInteger counter = new AtomicInteger();

                    @Override
                    public Thread newThread(@NonNull Runnable r) {
                        return new Thread(r, "documents-" + counter.incrementAndGet());
                    }
                },
                new ThreadPoolExecutor.CallerRunsPolicy()
        );
    }
}