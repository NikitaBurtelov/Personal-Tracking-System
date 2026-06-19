package org.pts.document.storage.config.mq;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.DefaultJacksonJavaTypeMapper;
import org.springframework.amqp.support.converter.JacksonJavaTypeMapper;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.json.JsonMapper;

@EnableRabbit
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
    public Binding uploadDocumentSourceCommandBinding(
            Queue uploadDocumentSourceCommandQueue,
            TopicExchange domainEventsExchange
    ) {
        return BindingBuilder
                .bind(uploadDocumentSourceCommandQueue)
                .to(domainEventsExchange)
                .with(properties.getUploadDocumentSourceCommandBinding().getRoutingKey());
    }

    @Bean
    public Binding deleteDocumentSourceCommandBinding(
            Queue deleteDocumentSourceCommandQueue,
            TopicExchange domainEventsExchange
    ) {
        return BindingBuilder
                .bind(deleteDocumentSourceCommandQueue)
                .to(domainEventsExchange)
                .with(properties.getDeleteDocumentSourceCommandBinding().getRoutingKey());
    }

    @Bean
    public Binding getDocumentSourceRequestBinding(
            Queue getDocumentSourceRequestQueue,
            TopicExchange domainEventsExchange
    ) {
        return BindingBuilder
                .bind(getDocumentSourceRequestQueue)
                .to(domainEventsExchange)
                .with(properties.getGetDocumentSourceRequestBinding().getRoutingKey());
    }

    @Bean
    public JsonMapper jsonMapper() {
        return JsonMapper.builder()
                .findAndAddModules()
                .build();
    }

    @Bean
    public JacksonJsonMessageConverter messageConverter(JsonMapper jsonMapper,
                                                        JacksonJavaTypeMapper typeMapper) {

        var converter = new JacksonJsonMessageConverter(jsonMapper);

        converter.setJavaTypeMapper(typeMapper);

        return converter;
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory,
            JacksonJsonMessageConverter converter) {

        SimpleRabbitListenerContainerFactory factory =
                new SimpleRabbitListenerContainerFactory();

        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(converter);

        return factory;
    }

    @Bean
    public JacksonJavaTypeMapper typeMapper() {
        DefaultJacksonJavaTypeMapper typeMapper = new DefaultJacksonJavaTypeMapper();

        typeMapper.setTrustedPackages("org.pts.document.storage.messaging.command.*");

        return typeMapper;
    }

    @Bean
    public RabbitTemplate rabbitTemplate(
            ConnectionFactory cf,
            JacksonJsonMessageConverter messageConverter
    ) {
        RabbitTemplate template = new RabbitTemplate(cf);
        template.setMessageConverter(messageConverter);
        return template;
    }
}
