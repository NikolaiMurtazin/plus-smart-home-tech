package ru.yandex.practicum.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecordBase;
import ru.yandex.practicum.config.KafkaProperties;
import ru.yandex.practicum.grpc.telemetry.event.SensorEventProto;
import ru.yandex.practicum.service.KafkaEventProducer;

@RequiredArgsConstructor
@Slf4j
public abstract class BaseSensorEventHandler<T extends SpecificRecordBase> implements SensorEventHandler {

    protected final KafkaEventProducer producer;
    protected final KafkaProperties kafkaProperties;

    protected abstract T mapToAvro(SensorEventProto event);

    @Override
    public void handle(SensorEventProto event) {
        T avroEvent = mapToAvro(event);
        String topic = kafkaProperties.getSensorEventsTopic();

        log.info("Отправка события {} в топик {}", getMessageType(), topic);
        producer.send(topic, event.getHubId(), avroEvent);
    }
}
