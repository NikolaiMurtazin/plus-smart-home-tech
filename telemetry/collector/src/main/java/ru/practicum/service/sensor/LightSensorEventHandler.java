package ru.practicum.service.sensor;

import org.springframework.stereotype.Service;
import ru.practicum.config.KafkaEventProducer;
import ru.practicum.config.KafkaTopics;
import ru.practicum.controller.BaseSensorEventHandler;
import ru.practicum.model.sensor.LightSensorEvent;
import ru.practicum.model.sensor.SensorEvent;
import ru.practicum.model.sensor.SensorEventType;
import ru.yandex.practicum.kafka.telemetry.event.LightSensorAvro;

@Service
public class LightSensorEventHandler extends BaseSensorEventHandler<LightSensorAvro> {

    public LightSensorEventHandler(KafkaEventProducer producer, KafkaTopics kafkaTopics) {
        super(producer, kafkaTopics);
    }

    @Override
    public SensorEventType getMessageType() {
        return SensorEventType.LIGHT_SENSOR_EVENT;
    }

    @Override
    protected LightSensorAvro mapToAvro(SensorEvent event) {
        LightSensorEvent lightEvent = (LightSensorEvent) event;

        return LightSensorAvro.newBuilder()
                .setLinkQuality(lightEvent.getLinkQuality())
                .setLuminosity(lightEvent.getLuminosity())
                .build();
    }
}
