package ru.yandex.practicum.evaluator;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.event.LightSensorAvro;
import ru.yandex.practicum.model.ConditionOperation;

@Component
public class LightConditionEvaluator extends ConditionEvaluator {
    @Override
    public boolean evaluate(Object sensorData, ConditionOperation operation, Integer value) {
        if (sensorData instanceof LightSensorAvro lightSensor) {
            return evaluateCondition(lightSensor.getLuminosity(), operation, value);
        }
        return false;
    }
}
