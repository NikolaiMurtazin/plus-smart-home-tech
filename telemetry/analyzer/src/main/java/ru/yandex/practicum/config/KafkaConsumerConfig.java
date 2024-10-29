package ru.yandex.practicum.config;

import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.yandex.practicum.deserializer.HubEventDeserializer;
import ru.yandex.practicum.deserializer.SensorsSnapshotDeserializer;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;

import java.util.Properties;

@Configuration
@RequiredArgsConstructor
public class KafkaConsumerConfig {

    private final KafkaSettings kafkaSettings;

    @Bean
    public KafkaConsumer<String, SensorsSnapshotAvro> kafkaConsumerSensorSnapshot() {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaSettings.getBootstrapServers());
        props.put(ConsumerConfig.GROUP_ID_CONFIG, kafkaSettings.getGroupIdTelemetrySnapshots());
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, kafkaSettings.getKeyDeserializer());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, SensorsSnapshotDeserializer.class);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, kafkaSettings.getAutoOffsetReset());

        return new KafkaConsumer<>(props);
    }

    @Bean
    public KafkaConsumer<String, HubEventAvro> kafkaConsumerHubEvent() {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaSettings.getBootstrapServers());
        props.put(ConsumerConfig.GROUP_ID_CONFIG, kafkaSettings.getGroupIdTelemetryHubs());
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, kafkaSettings.getKeyDeserializer());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, HubEventDeserializer.class);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, kafkaSettings.getAutoOffsetReset());

        return new KafkaConsumer<>(props);
    }
}
