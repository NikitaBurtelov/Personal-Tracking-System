package org.pts.document.storage.config.app;

import com.fasterxml.jackson.databind.json.JsonMapper;
import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.pts.document.storage.config.app.properties.DocumentStorageApplicationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

@Configuration
@RequiredArgsConstructor
public class DocumentStorageConfig {
    private final DocumentStorageApplicationProperties properties;

    @Bean
    @Primary
    public PlatformTransactionManager transactionManager(
            EntityManagerFactory emf) {
        return new JpaTransactionManager(emf);
    }

    @Bean
    public JsonMapper jsonMapper() {
        return JsonMapper.builder()
                .findAndAddModules()
                .build();
    }

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