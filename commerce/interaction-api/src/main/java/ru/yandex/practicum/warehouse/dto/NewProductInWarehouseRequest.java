package ru.yandex.practicum.warehouse.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NewProductInWarehouseRequest {
    @NotNull(message = "Идентификатор товара не может быть пустым.")
    private UUID productId;

    @NotNull(message = "Признак хрупкости не может быть пустым.")
    private boolean fragile;

    @NotNull(message = "Размеры товара не могут быть пустыми.")
    private DimensionDto dimension;

    @DecimalMin(value = "1.0", message = "Вес товара должен быть не менее 1.")
    private double weight;
}
