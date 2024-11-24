package ru.yandex.practicum.warehouse.dto;

import jakarta.validation.constraints.DecimalMin;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DimensionDto {
    @DecimalMin(value = "1.0", message = "Ширина должна быть не менее 1.")
    private double width;

    @DecimalMin(value = "1.0", message = "Высота должна быть не менее 1.")
    private double height;

    @DecimalMin(value = "1.0", message = "Глубина должна быть не менее 1.")
    private double depth;
}