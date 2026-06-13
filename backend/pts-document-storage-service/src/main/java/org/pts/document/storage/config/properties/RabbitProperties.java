package org.pts.document.storage.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Setter
@Getter
@ConfigurationProperties(prefix = "rabbit")
public class RabbitProperties {
    private Exchange exchange = new Exchange();
    private Queue documentStorageQueue = new Queue();
    private Binding documentStorageBinding = new Binding();

    @Getter
    @Setter
    public static class Exchange {
        private String name;
        private boolean durable = true;
        private boolean autoDelete = false;
    }

    @Getter
    @Setter
    public static class Queue {
        private String name;
        private boolean durable = true;
        private boolean exclusive = false;
        private boolean autoDelete = false;
    }

    @Getter
    @Setter
    public static class Binding {
        private String routingKey = "document.*";
    }
}