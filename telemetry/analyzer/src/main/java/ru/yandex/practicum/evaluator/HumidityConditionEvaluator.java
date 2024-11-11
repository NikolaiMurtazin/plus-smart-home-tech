package ru.yandex.practicum.evaluator;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.event.ClimateSensorAvro;
import ru.yandex.practicum.model.ConditionOperation;

@Component
public class HumidityConditionEvaluator extends ConditionEvaluator {
    @Override
    public boolean evaluate(Object sensorData, ConditionOperation operation, Integer value) {
        if (sensorData instanceof ClimateSensorAvro climateSensor) {
            return evaluateCondition(climateSensor.getHumidity(), operation, value);
        }
        return false;
    }
}
