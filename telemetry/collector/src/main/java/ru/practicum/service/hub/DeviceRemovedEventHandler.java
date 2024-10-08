package ru.practicum.service.hub;

import org.springframework.stereotype.Service;
import ru.practicum.config.KafkaEventProducer;
import ru.practicum.config.KafkaTopics;
import ru.practicum.controller.BaseHubEventHandler;
import ru.practicum.model.hub.DeviceRemovedEvent;
import ru.practicum.model.hub.HubEvent;
import ru.practicum.model.hub.HubEventType;
import ru.yandex.practicum.kafka.telemetry.event.DeviceRemovedEventAvro;

@Service
public class DeviceRemovedEventHandler extends BaseHubEventHandler<DeviceRemovedEventAvro> {

    public DeviceRemovedEventHandler(KafkaEventProducer producer, KafkaTopics kafkaTopics) {
        super(producer, kafkaTopics);
    }

    @Override
    public HubEventType getMessageType() {
        return HubEventType.DEVICE_REMOVED;
    }

    @Override
    protected DeviceRemovedEventAvro mapToAvro(HubEvent event) {
        DeviceRemovedEvent deviceRemovedEvent = (DeviceRemovedEvent) event;

        return DeviceRemovedEventAvro.newBuilder()
                .setId(deviceRemovedEvent.getId())
                .build();
    }
}
