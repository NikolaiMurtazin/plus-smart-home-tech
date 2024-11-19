package ru.yandex.practicum.service;

import ru.yandex.practicum.shoppingCart.dto.ChangeProductQuantityRequest;
import ru.yandex.practicum.shoppingCart.dto.ShoppingCartDto;
import ru.yandex.practicum.shoppingStore.dto.ProductDto;
import ru.yandex.practicum.warehouse.dto.BookedProductsDto;

import java.util.Map;
import java.util.UUID;

public interface ShoppingCartService {

    ShoppingCartDto getShoppingCart(String username);

    ShoppingCartDto addProducts(String username, Map<UUID, Integer> products);

    ShoppingCartDto removeProducts(String username, Map<UUID, Integer> products);

    ProductDto changeProductQuantity(String username, ChangeProductQuantityRequest request);

    void deactivateShoppingCart(String username);

    BookedProductsDto bookProducts(String username);
}
