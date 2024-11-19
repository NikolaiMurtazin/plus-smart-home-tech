package ru.yandex.practicum.service;

import ru.yandex.practicum.model.WarehouseProduct;
import ru.yandex.practicum.shoppingCart.dto.ShoppingCartDto;
import ru.yandex.practicum.warehouse.dto.AddProductToWarehouseRequest;
import ru.yandex.practicum.warehouse.dto.AddressDto;
import ru.yandex.practicum.warehouse.dto.AssemblyProductForOrderFromShoppingCartRequest;
import ru.yandex.practicum.warehouse.dto.BookedProductsDto;
import ru.yandex.practicum.warehouse.dto.NewProductInWarehouseRequest;

import java.util.Map;
import java.util.UUID;

public interface WarehouseService {
    WarehouseProduct addNewProduct(NewProductInWarehouseRequest request);

    WarehouseProduct addProductQuantity(AddProductToWarehouseRequest request);

    void acceptReturn(Map<UUID, Integer> products);

    BookedProductsDto bookProducts(ShoppingCartDto cartDto);

    BookedProductsDto assembleOrder(AssemblyProductForOrderFromShoppingCartRequest request);

    AddressDto getWarehouseAddress();
}
