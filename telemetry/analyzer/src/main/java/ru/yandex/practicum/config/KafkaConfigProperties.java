package ru.yandex.practicum.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "analyzer.kafka")
public class KafkaConfigProperties {

    private String bootstrapServers;
    private Consumer consumer;

    @Getter
    @Setter
    public static class Consumer {
        private String groupIdTelemetrySnapshot;
        private String groupIdTelemetryHub;
        private String keyDeserializer;
        private String valueDeserializerSnapshot;
        private String valueDeserializerHub;
        private String telemetrySnapshotsTopic;
        private String telemetryHubsTopic;
        private String pollTimeout;
        private boolean enableAutoCommit;
    }
}