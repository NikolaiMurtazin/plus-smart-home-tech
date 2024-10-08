package ru.practicum.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Getter
@Component
public class KafkaTopics {

    private final String telemetrySensors;
    private final String telemetryHubs;

    public KafkaTopics(
            @Value("${kafka.topics.telemetry-sensors}") String telemetrySensors,
            @Value("${kafka.topics.telemetry-hubs}") String telemetryHubs) {
        this.telemetrySensors = telemetrySensors;
        this.telemetryHubs = telemetryHubs;
    }
}
