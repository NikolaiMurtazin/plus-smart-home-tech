package ru.yandex.practicum.controller.sensor;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.config.KafkaProperties;
import ru.yandex.practicum.controller.BaseSensorEventHandler;
import ru.yandex.practicum.grpc.telemetry.event.ClimateSensorEvent;
import ru.yandex.practicum.grpc.telemetry.event.SensorEventProto;
import ru.yandex.practicum.kafka.telemetry.event.ClimateSensorAvro;
import ru.yandex.practicum.service.KafkaEventProducer;

@Component
public class ClimateSensorEventHandler extends BaseSensorEventHandler<ClimateSensorAvro> {
    public ClimateSensorEventHandler(KafkaEventProducer producer, KafkaProperties kafkaProperties) {
        super(producer, kafkaProperties);
    }

    @Override
    protected ClimateSensorAvro mapToAvro(SensorEventProto event) {
        ClimateSensorEvent sensorEvent = event.getClimateSensorEvent();
        return ClimateSensorAvro.newBuilder()
                .setHumidity(sensorEvent.getHumidity())
                .setCo2Level(sensorEvent.getCo2Level())
                .setTemperatureC(sensorEvent.getTemperatureC())
                .build();
    }

    @Override
    public SensorEventProto.PayloadCase getMessageType() {
        return SensorEventProto.PayloadCase.CLIMATE_SENSOR_EVENT;
    }
}
