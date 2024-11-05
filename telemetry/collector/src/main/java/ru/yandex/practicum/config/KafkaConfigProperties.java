package ru.yandex.practicum.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "collector.kafka")
public class KafkaConfigProperties {

    private String bootstrapServers;
    private Producer producer;

    @Getter
    @Setter
    public static class Producer {
        private String keySerializer;
        private String valueSerializer;
        private String telemetrySensorsTopic;
        private String telemetryHubsTopic;

    }
}
