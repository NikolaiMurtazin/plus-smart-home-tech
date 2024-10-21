package ru.yandex.practicum.service;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;

import java.time.Duration;
import java.util.Collections;

@Slf4j
@Component
@RequiredArgsConstructor
public class AggregationStarter {

    private final KafkaProducer<String, SensorsSnapshotAvro> producer;
    private final KafkaConsumer<String, SensorEventAvro> consumer;
    private final AggregatorService aggregatorService;

    @Value("${kafka.topics.telemetry-sensors}")
    private String telemetrySensors;

    @Value("${kafka.topic.telemetry-snapshots}")
    private String telemetrySnapshots;

    public void start() {
        try {
            consumer.subscribe(Collections.singletonList(telemetrySensors));
            log.info("Подписались на топик: {}", telemetrySensors);

            while (true) {
                ConsumerRecords<String, SensorEventAvro> records = consumer.poll(Duration.ofMillis(100));

                if (records.isEmpty()) {
                    continue;
                }

                for (ConsumerRecord<String, SensorEventAvro> record : records) {
                    SensorEventAvro event = record.value();

                    aggregatorService.updateState(event).ifPresent(snapshot -> {
                        try {
                            producer.send(new ProducerRecord<>(telemetrySnapshots, snapshot.getHubId(), snapshot), (metadata, exception) -> {
                                if (exception != null) {
                                    log.error("Ошибка при отправке снапшота в Kafka: {}", exception.getMessage());
                                    throw new RuntimeException("Ошибка при отправке сообщения в Kafka", exception);
                                }
                            });
                            log.info("Отправлен снапшот для хаба {} в топик {}", snapshot.getHubId(), telemetrySnapshots);
                        } catch (Exception e) {
                            log.error("Ошибка при отправке снапшота в топик", e);
                            throw new RuntimeException("Ошибка отправки сообщения в Kafka", e);
                        }
                    });
                }

                try {
                    consumer.commitSync();
                } catch (Exception e) {
                    log.error("Ошибка при коммите смещений", e);
                    throw new RuntimeException("Ошибка при коммите смещений в Kafka", e);
                }
            }
        } catch (Exception e) {
            log.error("Ошибка при обработке событий от датчиков", e);
            throw new RuntimeException("Ошибка при работе с Kafka", e);
        } finally {
            try {
                producer.flush();
                producer.close();
                consumer.close();
            } catch (Exception e) {
                log.error("Ошибка при закрытии продюсера или консьюмера", e);
                throw new RuntimeException("Ошибка при закрытии Kafka продюсера или консьюмера", e);
            }
        }
    }
}
