package ru.yandex.practicum;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.errors.WakeupException;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.consumer.KafkaProperties;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;
import ru.yandex.practicum.service.AnalyzerService;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class SnapshotProcessor implements Runnable {
    private final AnalyzerService analyzerProcessor;
    private final Consumer<String, SensorsSnapshotAvro> consumer;
    private final KafkaProperties kafkaProperties;
    private final Map<TopicPartition, OffsetAndMetadata> currentOffsets = new HashMap<>();
    private static int PERIOD_OF_MESSAGE_FIX;
    private static long POLL_DURATION;

    @PostConstruct
    private void init() {
        POLL_DURATION = kafkaProperties.getPollDuration();
        PERIOD_OF_MESSAGE_FIX = kafkaProperties.getMessageFixTime();
    }

    @Override
    public void run() {
        log.info("Snapshot processor started");
        Runtime.getRuntime().addShutdownHook(new Thread(consumer::wakeup));
        try {
            consumer.subscribe(List.of(kafkaProperties.getSnapshots().getTopic()));
            while (true) {
                ConsumerRecords<String, SensorsSnapshotAvro> poll = consumer.poll(Duration.ofMillis(POLL_DURATION));
                int count = 0;
                for (ConsumerRecord<String, SensorsSnapshotAvro> record : poll) {
                    analyzerProcessor.handleSnapshot(record.value());
                    manageOffsets(record, count, consumer);
                    count++;
                }
                consumer.commitAsync();
            }
        } catch (WakeupException ignores) {
        } catch (Exception e) {
            log.error("Ошибка во время обработки snapshot", e);
        } finally {
            try {
                consumer.commitSync(currentOffsets);
            } finally {
                log.info("Закрываем консьюмер");
                consumer.close();
            }
        }
    }

    private void manageOffsets(ConsumerRecord<String, SensorsSnapshotAvro> record, int count, Consumer<String, SensorsSnapshotAvro> consumer) {

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
}
