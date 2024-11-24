package ru.yandex.practicum.service;

import ru.yandex.practicum.shoppingStore.dto.PageableDto;
import ru.yandex.practicum.shoppingStore.dto.ProductDto;
import ru.yandex.practicum.shoppingStore.dto.SetProductQuantityStateRequest;
import ru.yandex.practicum.shoppingStore.enums.ProductCategory;

import java.util.List;
import java.util.UUID;

public interface ShoppingStoreService {
    List<ProductDto> getProductsByCategory(ProductCategory category, PageableDto pageableDto);
    ProductDto createNewProduct(ProductDto productDto);
    ProductDto updateProduct(ProductDto productDto);
    boolean removeProductFromStore(UUID productId);
    boolean setProductQuantityState(SetProductQuantityStateRequest request);
    ProductDto getProduct(UUID productId);
}
