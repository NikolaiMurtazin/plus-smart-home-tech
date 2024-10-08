package ru.practicum.service.sensor;

import org.springframework.stereotype.Service;
import ru.practicum.config.KafkaEventProducer;
import ru.practicum.config.KafkaTopics;
import ru.practicum.controller.BaseSensorEventHandler;
import ru.practicum.model.sensor.SensorEvent;
import ru.practicum.model.sensor.SensorEventType;
import ru.practicum.model.sensor.SwitchSensorEvent;
import ru.yandex.practicum.kafka.telemetry.event.SwitchSensorAvro;

@Service
public class SwitchSensorEventHandler extends BaseSensorEventHandler<SwitchSensorAvro> {

    public SwitchSensorEventHandler(KafkaEventProducer producer, KafkaTopics kafkaTopics) {
        super(producer, kafkaTopics);
    }

    @Override
    public SensorEventType getMessageType() {
        return SensorEventType.SWITCH_SENSOR_EVENT;
    }

    @Override
    protected SwitchSensorAvro mapToAvro(SensorEvent event) {
        SwitchSensorEvent switchEvent = (SwitchSensorEvent) event;

        return SwitchSensorAvro.newBuilder()
                .setState(switchEvent.isState())
                .build();
    }
}
