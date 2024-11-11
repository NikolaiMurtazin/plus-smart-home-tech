package ru.yandex.practicum.controller.hub;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.config.KafkaProperties;
import ru.yandex.practicum.controller.BaseHubEventHandler;
import ru.yandex.practicum.grpc.telemetry.event.DeviceAddedEventProto;
import ru.yandex.practicum.grpc.telemetry.event.DeviceTypeProto;
import ru.yandex.practicum.grpc.telemetry.event.HubEventProto;
import ru.yandex.practicum.kafka.telemetry.event.DeviceAddedEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.DeviceTypeAvro;
import ru.yandex.practicum.service.KafkaEventProducer;

@Service
public class DeviceAddedEventHandler extends BaseHubEventHandler<DeviceAddedEventAvro> {

    public DeviceAddedEventHandler(KafkaEventProducer producer, KafkaProperties kafkaTopics) {
        super(producer, kafkaTopics);
    }

    @Override
    public HubEventProto.PayloadCase getMessageType() {
        return HubEventProto.PayloadCase.DEVICE_ADDED;
    }

    @Override
    protected DeviceAddedEventAvro mapToAvro(HubEventProto event) {
        DeviceAddedEventProto deviceAddedEvent = event.getDeviceAdded();

        return DeviceAddedEventAvro.newBuilder()
                .setId(deviceAddedEvent.getId())
                .setType(mapDeviceTypeToAvro(deviceAddedEvent.getType()))
                .build();
    }

    private DeviceTypeAvro mapDeviceTypeToAvro(DeviceTypeProto deviceType) {
        return DeviceTypeAvro.valueOf(deviceType.name());
    }
}
