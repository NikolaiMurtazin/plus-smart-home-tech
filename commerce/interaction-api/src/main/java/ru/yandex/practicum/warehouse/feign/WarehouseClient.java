package ru.yandex.practicum.warehouse.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import ru.yandex.practicum.shoppingCart.dto.ShoppingCartDto;
import ru.yandex.practicum.warehouse.congif.WarehouseClientFallback;
import ru.yandex.practicum.warehouse.dto.AssemblyProductForOrderFromShoppingCartRequest;
import ru.yandex.practicum.warehouse.dto.BookedProductDto;


@FeignClient(name = "warehouse-service", path = "/api/v1/warehouse", fallback = WarehouseClientFallback.class)
public interface WarehouseClient {

    @PostMapping("/booking")
    BookedProductDto bookProducts(@RequestBody ShoppingCartDto shoppingCart);

    @PostMapping("/assembly")
    BookedProductDto assembleOrder(@RequestBody AssemblyProductForOrderFromShoppingCartRequest request);
}
