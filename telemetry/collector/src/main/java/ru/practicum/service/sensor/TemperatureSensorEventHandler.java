package ru.practicum.service.sensor;

import org.springframework.stereotype.Service;
import ru.practicum.config.KafkaEventProducer;
import ru.practicum.config.KafkaTopics;
import ru.practicum.controller.BaseSensorEventHandler;
import ru.practicum.model.sensor.SensorEvent;
import ru.practicum.model.sensor.SensorEventType;
import ru.practicum.model.sensor.TemperatureSensorEvent;
import ru.yandex.practicum.kafka.telemetry.event.TemperatureSensorAvro;

@Service
public class TemperatureSensorEventHandler extends BaseSensorEventHandler<TemperatureSensorAvro> {

    public TemperatureSensorEventHandler(KafkaEventProducer producer, KafkaTopics kafkaTopics) {
        super(producer, kafkaTopics);
    }

    @Override
    public SensorEventType getMessageType() {
        return SensorEventType.TEMPERATURE_SENSOR_EVENT;
    }

    @Override
    protected TemperatureSensorAvro mapToAvro(SensorEvent event) {
        TemperatureSensorEvent tempEvent = (TemperatureSensorEvent) event;

        return TemperatureSensorAvro.newBuilder()
                .setId(tempEvent.getId())
                .setHubId(tempEvent.getHubId())
                .setTimestamp(tempEvent.getTimestamp().toEpochMilli())
                .setTemperatureC(tempEvent.getTemperatureC())
                .setTemperatureF(tempEvent.getTemperatureF())
                .build();
    }
}
