package ru.yandex.practicum.kafka.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "kafka.topics")
public class KafkaTopics {

    private String telemetrySensors;
    private String telemetryHubs;

}
