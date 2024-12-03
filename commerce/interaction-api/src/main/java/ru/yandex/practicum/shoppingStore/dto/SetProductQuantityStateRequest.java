package ru.yandex.practicum.shoppingStore.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.yandex.practicum.shoppingStore.enums.QuantityState;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SetProductQuantityStateRequest {
    @NotNull(message = "Идентификатор товара не должен быть пустым")
    private UUID productId;

    @NotNull(message = "Статус количества не должен быть пустым")
    private QuantityState quantityState;
}
