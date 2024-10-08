package ru.practicum.service.sensor;

import org.springframework.stereotype.Service;
import ru.practicum.config.KafkaEventProducer;
import ru.practicum.config.KafkaTopics;
import ru.practicum.controller.BaseSensorEventHandler;
import ru.practicum.model.sensor.MotionSensorEvent;
import ru.practicum.model.sensor.SensorEvent;
import ru.practicum.model.sensor.SensorEventType;
import ru.yandex.practicum.kafka.telemetry.event.MotionSensorAvro;

@Service
public class MotionSensorEventHandler extends BaseSensorEventHandler<MotionSensorAvro> {

    public MotionSensorEventHandler(KafkaEventProducer producer, KafkaTopics kafkaTopics) {
        super(producer, kafkaTopics);
    }

    @Override
    public SensorEventType getMessageType() {
        return SensorEventType.MOTION_SENSOR_EVENT;
    }

    @Override
    protected MotionSensorAvro mapToAvro(SensorEvent event) {
        MotionSensorEvent motionEvent = (MotionSensorEvent) event;

        return MotionSensorAvro.newBuilder()
                .setLinkQuality(motionEvent.getLinkQuality())
                .setMotion(motionEvent.isMotion())
                .setVoltage(motionEvent.getVoltage())
                .build();
    }
}
