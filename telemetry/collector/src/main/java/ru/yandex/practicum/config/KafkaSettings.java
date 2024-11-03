package ru.yandex.practicum.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "spring.kafka")
public class KafkaSettings {
    private String bootstrapServers;
    private String schemaRegistryUrl;
    private String producerKeySerializer;
    private String topicsTelemetrySensors;
    private String topicsTelemetryHubs;
}
