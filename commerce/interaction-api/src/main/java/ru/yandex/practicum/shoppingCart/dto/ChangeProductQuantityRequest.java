package ru.yandex.practicum.shoppingCart.dto;

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
public class ChangeProductQuantityRequest {

    @NotNull(message = "Идентификатор товара не должен быть пустым")
    private UUID productId;

    @Min(value = 0, message = "Новое количество не должно быть меньше нуля")
    private int newQuantity;
}
