package ru.yandex.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
@RequiredArgsConstructor
@Slf4j
public class KafkaEventProducer {
    private final Producer<String, SpecificRecordBase> kafkaProducer;

    public <T extends SpecificRecordBase> void send(String topic, String key, T event) {
        ProducerRecord<String, SpecificRecordBase> record =
                new ProducerRecord<>(topic, 1, Instant.now().getEpochSecond(), key, event);

        kafkaProducer.send(record, (metadata, exception) -> {
            if (exception != null) {
                log.error("Ошибка при отправке сообщения в Kafka, topic: {}, key: {}", topic, key, exception);
            } else {
                log.info("Сообщение успешно отправлено в Kafka, topic: {}, partition: {}, offset: {}",
                        metadata.topic(), metadata.partition(), metadata.offset());
            }
        });
    }
}
