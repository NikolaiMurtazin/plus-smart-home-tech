package ru.yandex.practicum.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.exception.NotFoundException;
import ru.yandex.practicum.kafka.telemetry.event.DeviceActionAvro;
import ru.yandex.practicum.kafka.telemetry.event.ScenarioAddedEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.ScenarioConditionAvro;
import ru.yandex.practicum.model.*;
import ru.yandex.practicum.repository.ActionRepository;
import ru.yandex.practicum.repository.ConditionRepository;
import ru.yandex.practicum.repository.ScenarioRepository;
import ru.yandex.practicum.repository.SensorRepository;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ScenarioService {
    private final ScenarioRepository scenarioRepository;
    private final ConditionRepository conditionRepository;
    private final ActionRepository actionRepository;
    private final SensorRepository sensorRepository;

    public void addScenario(ScenarioAddedEventAvro event, String hubId) {
        checkSensorsExist(event, hubId);
        Scenario scenario = new Scenario();
        scenario.setName(event.getName());
        scenario.setHubId(hubId);

        List<Condition> conditions = event.getConditions().stream()
                .map(conditionEvent -> Condition.builder()
                        .sensorId(conditionEvent.getSensorId())
                        .type(ConditionType.valueOf(conditionEvent.getType().name()))
                        .operation(ConditionOperation.valueOf(conditionEvent.getOperation().name()))
                        .value(convertToInteger(conditionEvent.getValue()))
                        .scenario(scenario)
                        .build())
                .collect(Collectors.toList());

        List<Action> actions = event.getActions().stream()
                .map(actionEvent -> Action.builder()
                        .sensorId(actionEvent.getSensorId())
                        .type(ActionType.valueOf(actionEvent.getType().name()))
                        .value(actionEvent.getValue() != null ? actionEvent.getValue() : 0) //
                        .scenario(scenario)
                        .build())
                .collect(Collectors.toList());

        scenario.setConditions(conditions);
        scenario.setActions(actions);
        if (scenarioRepository.findByHubIdAndName(hubId, event.getName()).isPresent()) {
            conditionRepository.deleteAll(scenario.getConditions());
            actionRepository.deleteAll(scenario.getActions());
        }
        scenarioRepository.save(scenario);
    }

    private void checkSensorsExist(ScenarioAddedEventAvro event, String hubId) {
        Set<String> sensors = new HashSet<>();
        event.getActions().stream()
                .map(DeviceActionAvro::getSensorId)
                .forEach(sensors::add);
        event.getConditions().stream()
                .map(ScenarioConditionAvro::getSensorId)
                .forEach(sensors::add);

        boolean allSensorsExists = sensorRepository.existsByIdInAndHubId(sensors, hubId);
        if (!allSensorsExists) {
            throw new IllegalStateException("Невозможно создать сценарий с использованием неизвестного устройства");
        }
    }

    public void deleteScenario(String name, String hubId) {
        Optional<Scenario> savedScenario = scenarioRepository.findByHubIdAndName(hubId, name);
        savedScenario.orElseThrow(() -> new NotFoundException("Scenario with name not found"));
        Scenario scenario = savedScenario.get();
        scenarioRepository.delete(scenario);
        conditionRepository.deleteAll(scenario.getConditions());
        actionRepository.deleteAll(scenario.getActions());
    }

    private Integer convertToInteger(Object value) {
        if (value instanceof Integer) {
            return (Integer) value;
        } else if (value instanceof Boolean) {
            return (Boolean) value ? 1 : 0;
        } else {
            return null;
        }
    }
}