package org.pts.document.storage.config.app;

import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

@Configuration
@RequiredArgsConstructor
public class DocumentStorageConfig {

    @Bean
    public ExecutorService executorService() {

        return new ThreadPoolExecutor(
                10,
                10,
                0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(100),
                new ThreadFactory() {
                    private final AtomicInteger counter = new AtomicInteger();

                    @Override
                    public Thread newThread(@NonNull Runnable r) {
                        return new Thread(r, "upload-" + counter.incrementAndGet());
                    }
                },
                new ThreadPoolExecutor.CallerRunsPolicy()
        );
    }
}