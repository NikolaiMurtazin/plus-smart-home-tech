package ru.yandex.practicum.controller.sensor;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.config.KafkaProperties;
import ru.yandex.practicum.controller.BaseSensorEventHandler;
import ru.yandex.practicum.grpc.telemetry.event.SensorEventProto;
import ru.yandex.practicum.grpc.telemetry.event.TemperatureSensorEvent;
import ru.yandex.practicum.kafka.telemetry.event.TemperatureSensorAvro;
import ru.yandex.practicum.service.KafkaEventProducer;

@Component
public class TemperatureSensorEventHandler extends BaseSensorEventHandler<TemperatureSensorAvro> {
    public TemperatureSensorEventHandler(KafkaEventProducer producer, KafkaProperties kafkaProperties) {
        super(producer, kafkaProperties);
    }

    @Override
    protected TemperatureSensorAvro mapToAvro(SensorEventProto event) {
        TemperatureSensorEvent sensorEvent = event.getTemperatureSensorEvent();
        return TemperatureSensorAvro.newBuilder()
                .setTemperatureC(sensorEvent.getTemperatureC())
                .setTemperatureF(sensorEvent.getTemperatureF())
                .setHubId(event.getHubId())
                .setId(event.getId())
                .build();
    }

    @Override
    public SensorEventProto.PayloadCase getMessageType() {
        return SensorEventProto.PayloadCase.TEMPERATURE_SENSOR_EVENT;
    }
}
