package ru.yandex.practicum.warehouse.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import ru.yandex.practicum.shoppingCart.dto.ShoppingCartDto;
import ru.yandex.practicum.warehouse.dto.AssemblyProductForOrderFromShoppingCartRequest;
import ru.yandex.practicum.warehouse.dto.BookedProductsDto;


@FeignClient(name = "warehouse-service", url = "http://warehouse-service")
public interface WarehouseClient {

    @PostMapping("/api/v1/warehouse/booking")
    BookedProductsDto bookProducts(@RequestBody ShoppingCartDto shoppingCart);

    @PostMapping("/api/v1/warehouse/assembly")
    BookedProductsDto assembleOrder(@RequestBody AssemblyProductForOrderFromShoppingCartRequest request);
}
