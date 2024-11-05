package ru.yandex.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecordBase;
import ru.yandex.practicum.config.KafkaEventProducer;
import ru.yandex.practicum.config.KafkaConfigProperties;
import ru.yandex.practicum.grpc.telemetry.event.SensorEventProto;

@Slf4j
@RequiredArgsConstructor
public abstract class BaseSensorEventHandler<T extends SpecificRecordBase> implements SensorEventHandler {

    protected final KafkaEventProducer producer;
    protected final KafkaConfigProperties kafkaSettings;

    protected abstract T mapToAvro(SensorEventProto event);

    @Override
    public void handle(SensorEventProto event) {
        T protoEvent = mapToAvro(event);
        String topic = kafkaSettings.getProducer().getTelemetrySensorsTopic();

        log.info("Отправка события {} в топик {}", getMessageType(), topic);
        producer.send(topic, event.getId(), protoEvent);
    }
}
