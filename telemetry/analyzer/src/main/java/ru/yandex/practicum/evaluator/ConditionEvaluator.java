package ru.yandex.practicum.evaluator;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.model.ConditionOperation;

@Component
public abstract class ConditionEvaluator {
    public abstract boolean evaluate(Object sensorData, ConditionOperation operation, Integer value);

    public boolean evaluateCondition(int sensorValue, ConditionOperation operation, int targetValue) {
        ConditionOperation conditionOperation = ConditionOperation.valueOf(operation.name());
        return conditionOperation.apply(sensorValue, targetValue);
    }
}