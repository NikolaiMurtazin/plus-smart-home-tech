package ru.practicum.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.config.KafkaTopics;
import ru.practicum.model.sensor.SensorEvent;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;

@RestController
@RequiredArgsConstructor
@RequestMapping("/events")
@Slf4j
public class SensorEventController {
    private final KafkaProducer<String, SensorEventAvro> sensorEventProducer;

    @PostMapping("/sensors")
    public void collectSensorEvent(@Valid @RequestBody SensorEvent event) {
        log.info("Получено событие от датчика: {}", event);

        SensorEventAvro avroEvent = convertToAvro(event);

        log.debug("Преобразованное Avro-событие: {}", avroEvent);

        ProducerRecord<String, SensorEventAvro> record =
                new ProducerRecord<>(KafkaTopics.TELEMETRY_SENSORS_V1, event.getId(), avroEvent);
        sensorEventProducer.send(record, (metadata, exception) -> {
            if (exception != null) {
                log.error("Отправка в Kafka была прервана", exception);
            } else {
                log.info("Событие датчика успешно отправлено в Kafka, topic: {}, partition: {}, offset: {}",
                        metadata.topic(), metadata.partition(), metadata.offset());
            }
        });
    }

    private SensorEventAvro convertToAvro(SensorEvent event) {
        return new SensorEventAvro();
    }
}
