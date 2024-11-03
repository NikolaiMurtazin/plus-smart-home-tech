package ru.yandex.practicum.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "spring.kafka")
public class KafkaSettings {
    private String bootstrapServers;

    private String consumerGroupId;
    private String consumerAutoOffsetReset;
    private String consumerKeyDeserializer;

    private String producerKeySerializer;

    private String topicsTelemetrySensors;
    private String topicsTelemetrySnapshots;

    private String schemaRegistryUrl;
}
