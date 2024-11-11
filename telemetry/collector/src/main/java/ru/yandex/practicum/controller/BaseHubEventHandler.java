package ru.yandex.practicum.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecordBase;
import ru.yandex.practicum.config.KafkaProperties;
import ru.yandex.practicum.grpc.telemetry.event.HubEventProto;
import ru.yandex.practicum.model.hub.HubEvent;
import ru.yandex.practicum.service.KafkaEventProducer;

@Slf4j
@RequiredArgsConstructor
public abstract class BaseHubEventHandler<T extends SpecificRecordBase> implements HubEventHandler {

    protected final KafkaEventProducer producer;
    protected final KafkaProperties kafkaProperties;

    protected abstract T mapToAvro(HubEventProto event);

    @Override
    public void handle(HubEventProto event) {
        T protoEvent = mapToAvro(event);
        String topic = kafkaProperties.getHubEventsTopic();

        log.info("Отправка события {} в топик {}", getMessageType(), topic);
        producer.send(topic, event.getHubId(), protoEvent);
    }
}