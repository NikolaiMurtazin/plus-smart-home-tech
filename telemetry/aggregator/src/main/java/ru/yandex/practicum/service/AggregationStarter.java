package ru.yandex.practicum.service;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.errors.WakeupException;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.yandex.practicum.config.KafkaSettings;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorStateAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;

import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class AggregationStarter {

    private final KafkaConsumer<String, SensorEventAvro> consumer;
    private final KafkaProducer<String, SensorsSnapshotAvro> producer;
    private final KafkaSettings kafkaSettings;

    private final Map<String, SensorsSnapshotAvro> snapshots = new ConcurrentHashMap<>();


    public void start() {
        try {
            consumer.subscribe(Collections.singletonList(kafkaSettings.getTopicsTelemetrySensors()));

            log.info("Сервис агрегации запущен и подписан на топик [{}]", kafkaSettings.getTopicsTelemetrySensors());

            while (true) {
                ConsumerRecords<String, SensorEventAvro> records = consumer.poll(Duration.ofMillis(100));
                for (ConsumerRecord<String, SensorEventAvro> record : records) {
                    SensorEventAvro event = record.value();
                    processEvent(event);
                }
                consumer.commitAsync();
            }

        } catch (WakeupException ignored) {
            log.info("Получен сигнал остановки агрегации.");
        } catch (Exception e) {
            log.error("Ошибка во время обработки событий от датчиков", e);
        } finally {
            try {
                consumer.commitSync();
                producer.flush();
            } finally {
                log.info("Закрываем консьюмер");
                consumer.close();
                log.info("Закрываем продюсер");
                producer.close();
            }
        }
    }

    private void processEvent(SensorEventAvro event) {
        Optional<SensorsSnapshotAvro> updatedSnapshot = updateState(event);
        updatedSnapshot.ifPresent(this::sendSnapshot);
    }

    private Optional<SensorsSnapshotAvro> updateState(SensorEventAvro event) {
        String hubId = event.getHubId();
        String sensorId = event.getId();
        long eventTimestamp = event.getTimestamp();

        SensorsSnapshotAvro snapshot = snapshots.get(hubId);

        if (snapshot == null) {
            snapshot = SensorsSnapshotAvro.newBuilder()
                    .setHubId(hubId)
                    .setTimestamp(Instant.ofEpochSecond(eventTimestamp))
                    .setSensorsState(new HashMap<>())
                    .build();
        }

        SensorStateAvro oldState = snapshot.getSensorsState().get(sensorId);

        if (oldState != null
                && !oldState.getTimestamp().isBefore(Instant.ofEpochSecond(event.getTimestamp()))
                && oldState.getData().equals(event.getPayload())) {
            return Optional.empty();
        }

        SensorStateAvro newState = SensorStateAvro.newBuilder()
                .setTimestamp(Instant.ofEpochSecond(eventTimestamp))
                .setData(event.getPayload())
                .build();

        snapshot.getSensorsState().put(sensorId, newState);
        snapshot.setTimestamp(Instant.ofEpochSecond(eventTimestamp));

        snapshots.put(hubId, snapshot);

        return Optional.of(snapshot);
    }

    private void sendSnapshot(SensorsSnapshotAvro snapshot) {
        ProducerRecord<String, SensorsSnapshotAvro> record = new ProducerRecord<>(
                kafkaSettings.getTopicsTelemetrySensors(), snapshot.getHubId(), snapshot);

        producer.send(record, (metadata, exception) -> {
            if (exception != null) {
                log.error("Ошибка при отправке снапшота в Kafka", exception);
            } else {
                log.info("Снапшот отправлен в Kafka, hubId: {}", snapshot.getHubId());
            }
        });
    }

    public void shutdown() {
        consumer.wakeup();
    }
}
