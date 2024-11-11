package ru.yandex.practicum.controller.sensor;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.config.KafkaProperties;
import ru.yandex.practicum.controller.BaseSensorEventHandler;
import ru.yandex.practicum.grpc.telemetry.event.LightSensorEvent;
import ru.yandex.practicum.grpc.telemetry.event.SensorEventProto;
import ru.yandex.practicum.kafka.telemetry.event.LightSensorAvro;
import ru.yandex.practicum.service.KafkaEventProducer;

@Component
public class LightSensorEventHandler extends BaseSensorEventHandler<LightSensorAvro> {

    public LightSensorEventHandler(KafkaEventProducer producer, KafkaProperties kafkaProperties) {
        super(producer, kafkaProperties);
    }

    @Override
    protected LightSensorAvro mapToAvro(SensorEventProto event) {
        LightSensorEvent sensorEvent = event.getLightSensorEvent();
        return LightSensorAvro.newBuilder()
                .setLinkQuality(sensorEvent.getLinkQuality())
                .setLuminosity(sensorEvent.getLuminosity())
                .build();
    }

    @Override
    public SensorEventProto.PayloadCase getMessageType() {
        return SensorEventProto.PayloadCase.LIGHT_SENSOR_EVENT;
    }
}
