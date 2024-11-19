package ru.yandex.practicum.shoppingStore.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.yandex.practicum.shoppingStore.enums.QuantityState;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SetProductQuantityStateRequest {
    @NotNull(message = "Идентификатор товара не должен быть пустым")
    private UUID productId;

    @NotNull(message = "Статус количества не должен быть пустым")
    private QuantityState quantityState;
}
