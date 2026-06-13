package org.pts.document.storage.config;

import lombok.RequiredArgsConstructor;
import org.pts.document.storage.config.properties.RabbitProperties;
import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class RabbitConfig {
    private final RabbitProperties properties;

    @Bean
    public TopicExchange domainEventsExchange() {
        var exchange = properties.getExchange();

        return new TopicExchange(
                exchange.getName(),
                exchange.isDurable(),
                exchange.isAutoDelete()
        );
    }

    @Bean
    public Queue documentStorageQueue() {
        var queue = properties.getDocumentStorageQueue();

        return new Queue(
                queue.getName(),
                queue.isDurable(),
                queue.isExclusive(),
                queue.isAutoDelete()
        );
    }

    @Bean
    public Binding documentStorageBinding(
            Queue documentStorageQueue,
            TopicExchange domainEventsExchange
    ) {
        return BindingBuilder
                .bind(documentStorageQueue)
                .to(domainEventsExchange)
                .with(properties.getDocumentStorageBinding().getRoutingKey());
    }

    @Bean
    public MessageConverter jacksonConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
