package ru.yandex.practicum.controller.sensor;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.config.KafkaProperties;
import ru.yandex.practicum.controller.BaseSensorEventHandler;
import ru.yandex.practicum.grpc.telemetry.event.SensorEventProto;
import ru.yandex.practicum.grpc.telemetry.event.SwitchSensorEvent;
import ru.yandex.practicum.kafka.telemetry.event.SwitchSensorAvro;
import ru.yandex.practicum.service.KafkaEventProducer;

@Component
public class SwitchSensorEventHandler extends BaseSensorEventHandler<SwitchSensorAvro> {

    public SwitchSensorEventHandler(KafkaEventProducer producer, KafkaProperties kafkaTopics) {
        super(producer, kafkaTopics);
    }

    @Override
    public SensorEventProto.PayloadCase getMessageType() {
        return SensorEventProto.PayloadCase.SWITCH_SENSOR_EVENT;
    }

    @Override
    protected SwitchSensorAvro mapToAvro(SensorEventProto event) {
        SwitchSensorEvent switchEvent = event.getSwitchSensorEvent();

        return SwitchSensorAvro.newBuilder()
                .setState(switchEvent.getState())
                .build();
    }
}
