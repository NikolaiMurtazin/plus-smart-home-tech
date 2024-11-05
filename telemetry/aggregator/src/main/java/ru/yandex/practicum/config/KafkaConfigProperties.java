package ru.yandex.practicum.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;


@Getter
@Setter
@ConfigurationProperties(prefix = "aggregator.kafka")
public class KafkaConfigProperties {

    private String bootstrapServers;
    private Producer producer;
    private Consumer consumer;

    @Getter
    @Setter
    public static class Producer {
        private String keySerializer;
        private String valueSerializer;
        private String topic;
    }

    @Getter
    @Setter
    public static class Consumer {
        private String groupId;
        private String keyDeserializer;
        private String valueDeserializer;
        private String topic;
        private String pollTimeout;
        private boolean enableAutoCommit;
    }
}
