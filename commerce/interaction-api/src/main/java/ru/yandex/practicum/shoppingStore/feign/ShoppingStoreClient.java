package ru.yandex.practicum.shoppingStore.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import ru.yandex.practicum.shoppingStore.dto.ProductDto;

import java.util.UUID;

@FeignClient(name = "shopping-store")
public interface ShoppingStoreClient {

    @GetMapping("/{productId}")
    ProductDto getProduct(@PathVariable("productId") UUID productId);
}
