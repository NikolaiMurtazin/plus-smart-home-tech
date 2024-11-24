package ru.yandex.practicum.warehouse.dto;

import jakarta.validation.constraints.Min;
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
public class AddProductToWarehouseRequest {
    @NotNull(message = "Идентификатор товара не может быть пустым.")
    private UUID productId;

    @Min(value = 1, message = "Количество товара должно быть больше 0.")
    private int quantity;
}
