package ru.yandex.practicum;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.errors.WakeupException;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorStateAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;


@Component
@RequiredArgsConstructor
@Slf4j
public class AggregationStarter {
    private static int PERIOD_OF_MESSAGE_FIX;
    private static long POLL_DURATION;
    private final KafkaProperties kafkaProperties;
    private final Consumer<String, SensorEventAvro> consumer;
    private final Producer<String, SensorsSnapshotAvro> producer;
    private static final Map<TopicPartition, OffsetAndMetadata> currentOffsets = new HashMap<>();
    private final Map<String, SensorsSnapshotAvro> snapshots = new HashMap<>();


    @PostConstruct
    private void init() {
        POLL_DURATION = kafkaProperties.getConsumer().getPollDuration();
        PERIOD_OF_MESSAGE_FIX = kafkaProperties.getConsumer().getMessageFixTime();
    }

    public void start() {
        log.info("Aggregation started");
        Runtime.getRuntime().addShutdownHook(new Thread(consumer::wakeup));
        try {
            consumer.subscribe(List.of(kafkaProperties.getConsumer().getTopic()));
            while (true) {
                ConsumerRecords<String, SensorEventAvro> records = consumer.poll(Duration.ofMillis(POLL_DURATION));
                int count = 0;
                for (ConsumerRecord<String, SensorEventAvro> record : records) {
                    handleRecord(record);
                    manageOffsets(record, count, consumer);
                    count++;
                }
                consumer.commitAsync();
            }
        } catch (WakeupException | InterruptedException ignores) {
        } catch (Exception e) {
            log.error("Ошибка во время обработки событий от датчиков", e);
        } finally {
            try {
                producer.flush();
                consumer.commitSync(currentOffsets);
            } finally {
                log.info("Закрываем консьюмер");
                consumer.close();
                log.info("Закрываем продюсер");
                producer.close();
            }
        }
    }

    private static void manageOffsets(ConsumerRecord<String, SensorEventAvro> record, int count, Consumer<String, SensorEventAvro> consumer) {

        currentOffsets.put(
                new TopicPartition(record.topic(), record.partition()),
                new OffsetAndMetadata(record.offset() + 1)
        );

        if (count % PERIOD_OF_MESSAGE_FIX == 0) {
            consumer.commitAsync(currentOffsets, (offsets, exception) -> {
                if (exception != null) {
                    log.warn("Ошибка во время фиксации оффсетов: {}", offsets, exception);
                }
            });
        }
    }

    private void handleRecord(ConsumerRecord<String, SensorEventAvro> record) throws InterruptedException {
        SensorEventAvro event = record.value();
        Optional<SensorsSnapshotAvro> sensorsSnapshotAvro = updateState(event);
        if (sensorsSnapshotAvro.isPresent()) {
            SensorsSnapshotAvro sensorsSnapshot = sensorsSnapshotAvro.get();

            String topic = kafkaProperties.getProducer().getTopic();
            String key = sensorsSnapshot.getHubId();
            send(topic, key, sensorsSnapshot);
        }
    }

    private Optional<SensorsSnapshotAvro> updateState(SensorEventAvro event) {
        String sensorId = event.getId();
        String hubId = event.getHubId();
        Object payload = event.getPayload();
        Instant timestamp = Instant.ofEpochSecond(event.getTimestamp());
        SensorsSnapshotAvro snapshot = snapshots.get(hubId);
        if (snapshot == null) {

            snapshot = new SensorsSnapshotAvro(hubId, timestamp, new HashMap<>());
            snapshots.put(hubId, snapshot);
        }

        SensorStateAvro oldState = snapshot.getSensorsState().get(sensorId);
        if (oldState != null) {

            if (oldState.getTimestamp().isAfter(timestamp) ||
                    oldState.getData().equals(payload)) {

                return Optional.empty();
            }
        }

        SensorStateAvro newState = new SensorStateAvro(timestamp, payload);
        snapshot.getSensorsState().put(sensorId, newState);
        snapshot.setTimestamp(timestamp);

        return Optional.of(snapshot);
    }

    private void send(String topic, String key, SensorsSnapshotAvro snapshot) {
        ProducerRecord<String, SensorsSnapshotAvro> record =
                new ProducerRecord<>(topic, null, snapshot.getTimestamp().getEpochSecond(), key, snapshot);

        producer.send(record, (metadata, exception) -> {
            if (exception != null) {
                log.error("Ошибка при отправке сообщения в Kafka, topic: {}, key: {}", topic, key, exception);
            } else {
                log.info("Сообщение успешно отправлено в Kafka, topic: {}, partition: {}, offset: {}",
                        metadata.topic(), metadata.partition(), metadata.offset());
            }
        });
    }
}
