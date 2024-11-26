package ru.yandex.practicum.warehouse.congif;


import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.shoppingCart.dto.ShoppingCartDto;
import ru.yandex.practicum.warehouse.dto.AssemblyProductForOrderFromShoppingCartRequest;
import ru.yandex.practicum.warehouse.dto.BookedProductDto;
import ru.yandex.practicum.warehouse.feign.WarehouseClient;

@Component
@Slf4j
public class WarehouseClientFallback implements WarehouseClient {

    @Override
    public BookedProductDto bookProducts(ShoppingCartDto shoppingCart) {
        log.error("Ошибка при бронировании товаров для корзины: {}. Используем фолбэк.", shoppingCart);

        // Возвращаем заглушку с минимальной информацией
        return BookedProductDto.builder()
                .deliveryWeight(0.0)
                .deliveryVolume(0.0)
                .fragile(false)
                .build();
    }

    @Override
    public BookedProductDto assembleOrder(AssemblyProductForOrderFromShoppingCartRequest request) {
        log.error("Ошибка при сборке заказа для корзины с ID: {} и заказа с ID: {}. Используем фолбэк.",
                request.getShoppingCartId(), request.getOrderId());

        // Возвращаем заглушку с минимальной информацией
        return BookedProductDto.builder()
                .deliveryWeight(0.0)
                .deliveryVolume(0.0)
                .fragile(false)
                .build();
    }
}
