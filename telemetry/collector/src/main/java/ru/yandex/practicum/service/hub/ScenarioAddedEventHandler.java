package ru.yandex.practicum.service.hub;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.config.KafkaEventProducer;
import ru.yandex.practicum.config.KafkaSettings;
import ru.yandex.practicum.service.BaseHubEventHandler;
import ru.yandex.practicum.grpc.telemetry.event.DeviceActionProto;
import ru.yandex.practicum.grpc.telemetry.event.HubEventProto;
import ru.yandex.practicum.grpc.telemetry.event.ScenarioAddedEventProto;
import ru.yandex.practicum.grpc.telemetry.event.ScenarioConditionProto;
import ru.yandex.practicum.kafka.telemetry.event.ScenarioAddedEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.ScenarioConditionAvro;
import ru.yandex.practicum.kafka.telemetry.event.DeviceActionAvro;

import java.util.stream.Collectors;

@Service
public class ScenarioAddedEventHandler extends BaseHubEventHandler<ScenarioAddedEventAvro> {

    public ScenarioAddedEventHandler(KafkaEventProducer producer, KafkaSettings kafkaTopics) {
        super(producer, kafkaTopics);
    }

    @Override
    public HubEventProto.PayloadCase getMessageType() {
        return HubEventProto.PayloadCase.SCENARIO_ADDED;
    }

    @Override
    protected ScenarioAddedEventAvro mapToAvro(HubEventProto event) {
        ScenarioAddedEventProto scenarioEvent = event.getScenarioAdded();

        return ScenarioAddedEventAvro.newBuilder()
                .setName(scenarioEvent.getName())
                .setConditions(
                        scenarioEvent.getConditionList().stream()
                                .map(this::mapConditionToAvro)
                                .collect(Collectors.toList())
                )
                .setActions(
                        scenarioEvent.getActionList().stream()
                                .map(this::mapActionToAvro)
                                .collect(Collectors.toList())
                )
                .build();
    }

    private ScenarioConditionAvro mapConditionToAvro(ScenarioConditionProto condition) {
        return ScenarioConditionAvro.newBuilder()
                .setSensorId(condition.getSensorId())
                .setType(ru.yandex.practicum.kafka.telemetry.event.ConditionTypeAvro.valueOf(condition.getType().name()))
                .setOperation(ru.yandex.practicum.kafka.telemetry.event.ConditionOperationAvro.valueOf(condition.getOperation().name()))
                .setValue(condition.getBoolValue())
                .build();
    }

    private DeviceActionAvro mapActionToAvro(DeviceActionProto action) {
        return DeviceActionAvro.newBuilder()
                .setSensorId(action.getSensorId())
                .setType(ru.yandex.practicum.kafka.telemetry.event.ActionTypeAvro.valueOf(action.getType().name()))
                .setValue(action.getValue())
                .build();
    }
}
