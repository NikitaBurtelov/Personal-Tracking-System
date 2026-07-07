package org.pts.document.storage.config.kafka;

import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
@RequiredArgsConstructor
public class KafkaTopicConfig {
    private final KafkaProperties kafkaProperties;

    @Bean
    public NewTopic documentEventsTopic() {
        var topicSettings = kafkaProperties.getDocumentEventsTopic();

        return TopicBuilder.name(topicSettings.getName())
                .partitions(topicSettings.getPartitions())
                .replicas(topicSettings.getReplicas())
                .build();
    }

    @Bean
    public NewTopic documentCommandsTopic() {
        var topicSettings = kafkaProperties.getDocumentCommandsTopic();

        return TopicBuilder.name(topicSettings.getName())
                .partitions(topicSettings.getPartitions())
                .replicas(topicSettings.getReplicas())
                .build();
    }
}
