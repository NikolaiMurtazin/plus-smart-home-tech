package ru.yandex.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecordBase;
import ru.yandex.practicum.config.KafkaEventProducer;
import ru.yandex.practicum.config.KafkaSettings;
import ru.yandex.practicum.grpc.telemetry.event.HubEventProto;

@Slf4j
@RequiredArgsConstructor
public abstract class BaseHubEventHandler<T extends SpecificRecordBase> implements HubEventHandler {

    protected final KafkaEventProducer producer;
    protected final KafkaSettings kafkaSettings;

    protected abstract T mapToAvro(HubEventProto event);

    @Override
    public void handle(HubEventProto event) {
        T protoEvent = mapToAvro(event);
        String topic = kafkaSettings.getTopicsTelemetryHubs();

        log.info("Отправка события {} в топик {}", getMessageType(), topic);
        producer.send(topic, event.getHubId(), protoEvent);
    }
}
