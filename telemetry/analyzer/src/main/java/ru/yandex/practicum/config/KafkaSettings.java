package ru.yandex.practicum.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "spring.kafka")
public class KafkaSettings {
    private String bootstrapServers;
    private String groupIdTelemetrySnapshots;
    private String groupIdTelemetryHubs;
    private String keyDeserializer;
    private String autoOffsetReset;
    private int pollTimeout;
    private String schemaRegistryUrl;
    private String topicsTelemetrySnapshots;
    private String topicsTelemetryHubs;
}
