package ru.yandex.practicum.evaluator;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.event.TemperatureSensorAvro;
import ru.yandex.practicum.model.ConditionOperation;

@Component
public class TemperatureConditionEvaluator extends ConditionEvaluator {
    @Override
    public boolean evaluate(Object sensorData, ConditionOperation operation, Integer value) {
        if (sensorData instanceof TemperatureSensorAvro tempSensor) {
            return evaluateCondition(tempSensor.getTemperatureC(), operation, value);
        }
        return false;
    }
}
