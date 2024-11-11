package ru.yandex.practicum.controller;

import ru.yandex.practicum.grpc.telemetry.event.SensorEventProto;

public interface SensorEventHandler {
    void handle(SensorEventProto sensorEvent);

    SensorEventProto.PayloadCase getMessageType();

}
