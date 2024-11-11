package ru.yandex.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.controller.HubRouterController;
import ru.yandex.practicum.evaluator.ConditionEvaluator;
import ru.yandex.practicum.evaluator.ConditionEvaluatorFactory;
import ru.yandex.practicum.kafka.telemetry.event.SensorStateAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;
import ru.yandex.practicum.model.Action;
import ru.yandex.practicum.model.Condition;
import ru.yandex.practicum.model.Scenario;
import ru.yandex.practicum.repository.ScenarioRepository;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnalyzerService {
    private final ScenarioRepository scenarioRepository;
    private final HubRouterController hubRouterController;

    public void handleSnapshot(SensorsSnapshotAvro snapshot) {
        String hubId = snapshot.getHubId();
        log.info("Processing snapshot for hubId: {}", hubId);

        List<Scenario> scenarios = scenarioRepository.findByHubId(hubId);

        scenarios.stream()
                .filter(scenario -> isScenarioTriggered(scenario, snapshot))
                .forEach(scenario -> executeActions(scenario, hubId));
    }

    private boolean isScenarioTriggered(Scenario scenario, SensorsSnapshotAvro snapshot) {
        return scenario.getConditions().stream()
                .allMatch(condition -> checkCondition(condition, snapshot));
    }

    private boolean checkCondition(Condition condition, SensorsSnapshotAvro snapshot) {
        SensorStateAvro sensorState = snapshot.getSensorsState().get(condition.getSensorId());
        try {
            if (sensorState == null) {
                log.warn("Sensor data for sensorId {} is missing in the snapshot", condition.getSensorId());
                return false;
            }

            Object sensorData = sensorState.getData();
            ConditionEvaluator evaluator = ConditionEvaluatorFactory.getEvaluator(condition.getType());
            return evaluator.evaluate(sensorData, condition.getOperation(), condition.getValue());

        } catch (Exception e) {
            log.error("Error checking condition {}: {}", condition, e.getMessage());
        }
        return false;
    }

    private void executeActions(Scenario scenario, String hubId) {
        List<Action> actions = scenario.getActions();
        for (Action action : actions) {
            hubRouterController.doAction(scenario, action, hubId);
            log.info("Executing action: {} for hubId: {}", action, hubId);
        }
    }
}