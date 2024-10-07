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
        log.info("Received sensor event: {}", event);

        // Преобразование JSON-события в Avro-сообщение
        SensorEventAvro avroEvent = convertToAvro(event);

        // Логгируем перед отправкой
        log.debug("Converted Avro event: {}", avroEvent);

        // Отправка в Kafka
        ProducerRecord<String, SensorEventAvro> record =
                new ProducerRecord<>(KafkaTopics.TELEMETRY_SENSORS_V1, event.getId(), avroEvent);
        sensorEventProducer.send(record, (metadata, exception) -> {
            if (exception != null) {
                log.error("Failed to send sensor event to Kafka", exception);
            } else {
                log.info("Sensor event sent to Kafka, topic: {}, partition: {}, offset: {}",
                        metadata.topic(), metadata.partition(), metadata.offset());
            }
        });
    }

    private SensorEventAvro convertToAvro(SensorEvent event) {
        // Реализуйте преобразование JSON-события в Avro-объект
        return new SensorEventAvro(); // Пример, заполните по необходимости
    }
}
