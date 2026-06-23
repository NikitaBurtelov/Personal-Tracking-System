package org.pts.document.storage.config.app.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties("application")
public class DocumentStorageApplicationProperties {
    private ExecutorSettings uploadDocumentProcessExecutorSettings = new ExecutorSettings();
    private ExecutorSettings getDocumentProcessExecutorSettings = new ExecutorSettings();
    private ExecutorSettings documentServiceExecutorSettings = new ExecutorSettings();

    private SemaphoreSettings uploadDocumentProcessSemaphoreSettings = new SemaphoreSettings();
    private SemaphoreSettings getDocumentProcessSemaphoreSettings = new SemaphoreSettings();
    private SemaphoreSettings deleteDocumentProcessSemaphoreSettings = new SemaphoreSettings();
    private SemaphoreSettings updateJobStatusProcessSemaphoreSettings = new SemaphoreSettings();

    @Setter
    @Getter
    public static class ExecutorSettings {
        private int corePoolSize;
        private int maxPoolSize;
        private int queueCapacity;
    }

    @Getter
    @Setter
    public static class SemaphoreSettings {
        private int permits;
    }
}
