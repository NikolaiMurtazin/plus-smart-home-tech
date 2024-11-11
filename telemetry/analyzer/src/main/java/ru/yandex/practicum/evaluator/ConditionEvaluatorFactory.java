package ru.yandex.practicum.evaluator;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.model.ConditionType;

import java.util.HashMap;
import java.util.Map;

@Component
public class ConditionEvaluatorFactory {

    private static final Map<ConditionType, ConditionEvaluator> evaluators = new HashMap<>();

    static {
        evaluators.put(ConditionType.TEMPERATURE, new TemperatureConditionEvaluator());
        evaluators.put(ConditionType.HUMIDITY, new HumidityConditionEvaluator());
        evaluators.put(ConditionType.CO2LEVEL, new AirConditionEvaluator());
        evaluators.put(ConditionType.MOTION, new MotionConditionEvaluator());
        evaluators.put(ConditionType.SWITCH, new SwitchConditionEvaluator());
        evaluators.put(ConditionType.LUMINOSITY, new LightConditionEvaluator());
    }

    public static <T> ConditionEvaluator getEvaluator(ConditionType type) {
        return evaluators.get(type);
    }
}
