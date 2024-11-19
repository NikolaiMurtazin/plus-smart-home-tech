package ru.yandex.practicum.warehouse.dto;

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
public class AssemblyProductForOrderFromShoppingCartRequest {
    @NotNull(message = "Идентификатор корзины не может быть пустым.")
    private UUID shoppingCartId;

    @NotNull(message = "Идентификатор заказа не может быть пустым.")
    private UUID orderId;
}
