package ru.practicum.service.hub;

import org.springframework.stereotype.Service;
import ru.practicum.config.KafkaEventProducer;
import ru.practicum.config.KafkaTopics;
import ru.practicum.controller.BaseHubEventHandler;
import ru.practicum.model.hub.DeviceAction;
import ru.practicum.model.hub.ScenarioAddedEvent;
import ru.practicum.model.hub.HubEvent;
import ru.practicum.model.hub.HubEventType;
import ru.practicum.model.hub.ScenarioCondition;
import ru.yandex.practicum.kafka.telemetry.event.ScenarioAddedEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.ScenarioConditionAvro;
import ru.yandex.practicum.kafka.telemetry.event.DeviceActionAvro;

import java.util.stream.Collectors;

@Service
public class ScenarioAddedEventHandler extends BaseHubEventHandler<ScenarioAddedEventAvro> {

    public ScenarioAddedEventHandler(KafkaEventProducer producer, KafkaTopics kafkaTopics) {
        super(producer, kafkaTopics);
    }

    @Override
    public HubEventType getMessageType() {
        return HubEventType.SCENARIO_ADDED;
    }

    @Override
    protected ScenarioAddedEventAvro mapToAvro(HubEvent event) {
        ScenarioAddedEvent scenarioEvent = (ScenarioAddedEvent) event;

        return ScenarioAddedEventAvro.newBuilder()
                .setName(scenarioEvent.getName())
                .setConditions(
                        scenarioEvent.getConditions().stream()
                                .map(this::mapConditionToAvro)
                                .collect(Collectors.toList())
                )
                .setActions(
                        scenarioEvent.getActions().stream()
                                .map(this::mapActionToAvro)
                                .collect(Collectors.toList())
                )
                .build();
    }

    private ScenarioConditionAvro mapConditionToAvro(ScenarioCondition condition) {
        return ScenarioConditionAvro.newBuilder()
                .setSensorId(condition.getSensorId())
                .setType(ru.yandex.practicum.kafka.telemetry.event.ConditionTypeAvro.valueOf(condition.getType().name()))
                .setOperation(ru.yandex.practicum.kafka.telemetry.event.ConditionOperationAvro.valueOf(condition.getOperation().name()))
                .setValue(condition.getValue())
                .build();
    }

    private DeviceActionAvro mapActionToAvro(DeviceAction action) {
        return DeviceActionAvro.newBuilder()
                .setSensorId(action.getSensorId())
                .setType(ru.yandex.practicum.kafka.telemetry.event.ActionTypeAvro.valueOf(action.getType().name()))
                .setValue(action.getValue())
                .build();
    }
}
