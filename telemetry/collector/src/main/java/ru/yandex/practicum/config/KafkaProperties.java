package ru.yandex.practicum.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties("spring.kafka")
public class KafkaProperties {
    private String bootstrapServers;
    private Producer producer;
    @Value("${collector.kafka.topics.hubEvents}")
    private String hubEventsTopic;
    @Value("${collector.kafka.topics.sensorEvents}")
    private String sensorEventsTopic;

    @Getter
    @Setter
    public static class Producer {
        String keySerializer;
        String valueSerializer;
    }
}
