package org.pts.document.storage.config.mq;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class RabbitQueueConfig {
    private final RabbitProperties properties;

    @Bean
    public Queue uploadDocumentSourceCommandQueue() {
        var queue = properties.getUploadDocumentSourceCommandQueue();

        return new Queue(
                queue.getName(),
                queue.isDurable(),
                queue.isExclusive(),
                queue.isAutoDelete()
        );
    }

    @Bean
    public Queue deleteDocumentSourceCommandQueue() {
        var queue = properties.getDeleteDocumentSourceCommandQueue();

        return new Queue(
                queue.getName(),
                queue.isDurable(),
                queue.isExclusive(),
                queue.isAutoDelete()
        );
    }

    @Bean
    public Queue getDocumentSourceRequestQueue() {
        var queue = properties.getGetDocumentSourceRequestQueue();

        return new Queue(
                queue.getName(),
                queue.isDurable(),
                queue.isExclusive(),
                queue.isAutoDelete()
        );
    }
}
