package ru.yandex.practicum.service;

import ru.yandex.practicum.shoppingCart.dto.ShoppingCartDto;
import ru.yandex.practicum.warehouse.dto.AddProductToWarehouseRequest;
import ru.yandex.practicum.warehouse.dto.AddressDto;
import ru.yandex.practicum.warehouse.dto.AssemblyProductForOrderFromShoppingCartRequest;
import ru.yandex.practicum.warehouse.dto.BookedProductDto;
import ru.yandex.practicum.warehouse.dto.NewProductInWarehouseRequest;

import java.util.Map;
import java.util.UUID;

public interface WarehouseService {
    void addNewProduct(NewProductInWarehouseRequest request);

    void acceptReturn(Map<UUID, Integer> products);

    BookedProductDto bookProductForShoppingCart(ShoppingCartDto cartDto);

    BookedProductDto assemblyProductForOrderFromShoppingCart(AssemblyProductForOrderFromShoppingCartRequest request);

    void addProductQuantity(AddProductToWarehouseRequest request);

    AddressDto getWarehouseAddress();
}
