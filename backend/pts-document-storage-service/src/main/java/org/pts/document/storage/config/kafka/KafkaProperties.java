package org.pts.document.storage.config.kafka;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.kafka.topic")
public class KafkaProperties {
    private Topic documentEventsTopic = new Topic();
    private Topic documentCommandsTopic = new Topic();

    @Getter
    @Setter
    public static class Topic {
        private String name;
        private int partitions;
        private int replicas;
    }
}
