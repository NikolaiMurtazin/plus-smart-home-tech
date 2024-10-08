package ru.practicum.service.sensor;

import org.springframework.stereotype.Service;
import ru.practicum.config.KafkaEventProducer;
import ru.practicum.config.KafkaTopics;
import ru.practicum.controller.BaseSensorEventHandler;
import ru.practicum.model.sensor.ClimateSensorEvent;
import ru.practicum.model.sensor.SensorEvent;
import ru.practicum.model.sensor.SensorEventType;
import ru.yandex.practicum.kafka.telemetry.event.ClimateSensorAvro;

@Service
public class ClimateSensorEventHandler extends BaseSensorEventHandler<ClimateSensorAvro> {

    public ClimateSensorEventHandler(KafkaEventProducer producer, KafkaTopics kafkaTopics) {
        super(producer, kafkaTopics);
    }

    @Override
    public SensorEventType getMessageType() {
        return SensorEventType.CLIMATE_SENSOR_EVENT;
    }

    @Override
    protected ClimateSensorAvro mapToAvro(SensorEvent event) {
        ClimateSensorEvent climateEvent = (ClimateSensorEvent) event;

        return ClimateSensorAvro.newBuilder()
                .setTemperatureC(climateEvent.getTemperatureC())
                .setHumidity(climateEvent.getHumidity())
                .setCo2Level(climateEvent.getCo2Level())
                .build();
    }
}
