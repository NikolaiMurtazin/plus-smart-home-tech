package ru.practicum.model.hub.scenario.added.action;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class DeviceAction {
    @NotBlank
    private String sensorId;

    @NotBlank
    private ActionType type; // Возможные значения: ACTIVATE, DEACTIVATE, INVERSE, SET_VALUE

    @NotNull
    private Integer value; // Необязательное значение, например, уровень яркости, если это SET_VALUE
}