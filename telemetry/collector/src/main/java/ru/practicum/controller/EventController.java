package ru.practicum.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.model.hub.HubEvent;
import ru.practicum.model.hub.HubEventType;
import ru.practicum.model.sensor.SensorEvent;
import ru.practicum.model.sensor.SensorEventType;


import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/events")
@Slf4j
public class EventController {

    private final Map<SensorEventType, SensorEventHandler> sensorEventHandlers;
    private final Map<HubEventType, HubEventHandler> hubEventHandlers;

    public EventController(Set<SensorEventHandler> sensorEventHandlers, Set<HubEventHandler> hubEventHandlers) {
        this.sensorEventHandlers = sensorEventHandlers.stream()
                .collect(Collectors.toMap(SensorEventHandler::getMessageType, Function.identity()));
        this.hubEventHandlers = hubEventHandlers.stream()
                .collect(Collectors.toMap(HubEventHandler::getMessageType, Function.identity()));
    }

    @PostMapping("/sensors")
    public void collectSensorEvent(@Valid @RequestBody SensorEvent event) {
        log.info("Получено событие от датчика: {}", event);

        SensorEventHandler handler = sensorEventHandlers.get(event.getType());
        if (handler != null) {
            handler.handle(event);
        } else {
            log.warn("Обработчик для типа события {} не найден", event.getType());
        }
    }

    @PostMapping("/hubs")
    public void collectHubEvent(@Valid @RequestBody HubEvent event) {
        log.info("Получено событие от хаба: {}", event);

        HubEventHandler handler = hubEventHandlers.get(event.getType());
        if (handler != null) {
            handler.handle(event);
        } else {
            log.warn("Обработчик для типа события {} не найден", event.getType());
        }
    }
}
