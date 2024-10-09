package ru.practicum.service.hub;

import org.springframework.stereotype.Service;
import ru.practicum.config.KafkaEventProducer;
import ru.practicum.config.KafkaTopics;
import ru.practicum.controller.BaseHubEventHandler;
import ru.practicum.model.hub.DeviceAddedEvent;
import ru.practicum.model.hub.DeviceType;
import ru.practicum.model.hub.HubEvent;
import ru.practicum.model.hub.HubEventType;
import ru.yandex.practicum.kafka.telemetry.event.DeviceAddedEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.DeviceTypeAvro;

@Service
public class DeviceAddedEventHandler extends BaseHubEventHandler<DeviceAddedEventAvro> {

    public DeviceAddedEventHandler(KafkaEventProducer producer, KafkaTopics kafkaTopics) {
        super(producer, kafkaTopics);
    }

    @Override
    public HubEventType getMessageType() {
        return HubEventType.DEVICE_ADDED;
    }

    @Override
    protected DeviceAddedEventAvro mapToAvro(HubEvent event) {
        DeviceAddedEvent deviceAddedEvent = (DeviceAddedEvent) event;

        return DeviceAddedEventAvro.newBuilder()
                .setId(deviceAddedEvent.getId())
                .setType(mapDeviceTypeToAvro(deviceAddedEvent.getDeviceType()))
                .build();
    }

    private DeviceTypeAvro mapDeviceTypeToAvro(DeviceType deviceType) {
        return DeviceTypeAvro.valueOf(deviceType.name());
    }
}
