package ru.yandex.practicum.kafka.config;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.avro.specific.SpecificRecordBase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class KafkaEventProducer {
    private final KafkaProducer<String, SpecificRecordBase> kafkaProducer;

    public <T extends SpecificRecordBase> void send(String topic, String key, T event) {
        ProducerRecord<String, SpecificRecordBase> record =
                new ProducerRecord<>(topic, key, event);

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
