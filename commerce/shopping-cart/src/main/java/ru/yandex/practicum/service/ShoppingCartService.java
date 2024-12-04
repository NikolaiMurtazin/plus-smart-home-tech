package ru.yandex.practicum.service;

import ru.yandex.practicum.shoppingCart.dto.ChangeProductQuantityRequest;
import ru.yandex.practicum.shoppingCart.dto.ShoppingCartDto;
import ru.yandex.practicum.warehouse.dto.BookedProductDto;

import java.util.Map;
import java.util.UUID;

public interface ShoppingCartService {

    ShoppingCartDto getShoppingCart(String username);

    ShoppingCartDto addProducts(String username, Map<UUID, Integer> products);

    void deactivateShoppingCart(String username);

    ShoppingCartDto changeProductQuantity(String username, ChangeProductQuantityRequest request);

    ShoppingCartDto removeProducts(String username, Map<UUID, Integer> products);

    BookedProductDto bookProducts(String username);
}
