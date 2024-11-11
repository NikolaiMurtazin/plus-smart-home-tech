package ru.yandex.practicum.controller.sensor;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.config.KafkaProperties;
import ru.yandex.practicum.controller.BaseSensorEventHandler;
import ru.yandex.practicum.grpc.telemetry.event.MotionSensorEvent;
import ru.yandex.practicum.grpc.telemetry.event.SensorEventProto;
import ru.yandex.practicum.kafka.telemetry.event.MotionSensorAvro;
import ru.yandex.practicum.service.KafkaEventProducer;

@Component
public class MotionSensorEventHandler extends BaseSensorEventHandler<MotionSensorAvro> {
    public MotionSensorEventHandler(KafkaEventProducer producer, KafkaProperties kafkaProperties) {
        super(producer, kafkaProperties);
    }

    @Override
    protected MotionSensorAvro mapToAvro(SensorEventProto event) {
        MotionSensorEvent sensorEvent = event.getMotionSensorEvent();
        return MotionSensorAvro.newBuilder()
                .setMotion(sensorEvent.getMotion())
                .setLinkQuality(sensorEvent.getLinkQuality())
                .setVoltage(sensorEvent.getVoltage())
                .build();
    }

    @Override
    public SensorEventProto.PayloadCase getMessageType() {
        return SensorEventProto.PayloadCase.MOTION_SENSOR_EVENT;
    }
}
