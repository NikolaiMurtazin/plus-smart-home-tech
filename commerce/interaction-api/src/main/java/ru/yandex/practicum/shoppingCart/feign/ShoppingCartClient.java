package ru.yandex.practicum.shoppingCart.feign;

import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name = "shopping-cart-service", path = "/api/v1/shopping-cart")
public interface ShoppingCartClient {
}
