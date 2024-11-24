package ru.yandex.practicum.consumer;


import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "spring.kafka.consumer")
public class KafkaProperties {
    private String bootstrapServers;
    private long pollDuration;
    private int messageFixTime;
    private Hubs hubs;
    private Snapshots snapshots;

    @Getter
    @Setter
    public static class ConsumerFields {
        private String bootstrapServers;
        private String groupId;
        private String keyDeserializer;
        private String valueSerializer;
        private String topic;
    }

    @Getter
    @Setter
    public static class Hubs extends ConsumerFields {
    }

    @Getter
    @Setter
    public static class Snapshots extends ConsumerFields {
    }

}
