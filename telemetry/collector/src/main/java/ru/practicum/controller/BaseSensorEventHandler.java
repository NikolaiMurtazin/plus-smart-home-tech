package ru.practicum.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecordBase;
import ru.practicum.config.KafkaEventProducer;
import ru.practicum.config.KafkaTopics;
import ru.practicum.model.sensor.SensorEvent;


@Slf4j
@RequiredArgsConstructor
public abstract class BaseSensorEventHandler<T extends SpecificRecordBase> implements SensorEventHandler {

    protected final KafkaEventProducer producer;
    protected final KafkaTopics kafkaTopics;

    protected abstract T mapToAvro(SensorEvent event);

    @Override
    public void handle(SensorEvent event) {
        T avroEvent = mapToAvro(event);
        String topic = kafkaTopics.getTelemetrySensors();

        log.info("Отправка события {} в топик {}", getMessageType(), topic);
        producer.send(topic, event.getId(), avroEvent);
    }
}
