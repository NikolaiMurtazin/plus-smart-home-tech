package ru.yandex.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.exeption.NoSpecifiedProductInWarehouseException;
import ru.yandex.practicum.exeption.ProductInShoppingCartLowQuantityInWarehouse;
import ru.yandex.practicum.exeption.SpecifiedProductAlreadyInWarehouseException;
import ru.yandex.practicum.mapper.WarehouseMapper;
import ru.yandex.practicum.model.WarehouseProduct;
import ru.yandex.practicum.repository.WarehouseProductRepository;
import ru.yandex.practicum.shoppingCart.dto.ShoppingCartDto;
import ru.yandex.practicum.shoppingCart.feign.ShoppingCartClient;
import ru.yandex.practicum.shoppingStore.dto.ProductDto;
import ru.yandex.practicum.shoppingStore.feign.ShoppingStoreClient;
import ru.yandex.practicum.warehouse.dto.AddProductToWarehouseRequest;
import ru.yandex.practicum.warehouse.dto.AddressDto;
import ru.yandex.practicum.warehouse.dto.AssemblyProductForOrderFromShoppingCartRequest;
import ru.yandex.practicum.warehouse.dto.BookedProductsDto;
import ru.yandex.practicum.warehouse.dto.NewProductInWarehouseRequest;

import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class WarehouseServiceImpl implements WarehouseService {

    private final WarehouseProductRepository warehouseProductRepository;
    private final ShoppingStoreClient shoppingStoreClient;
    private final ShoppingCartClient shoppingCartClient;
    private final WarehouseMapper warehouseMapper;

    @Override
    public WarehouseProduct addNewProduct(NewProductInWarehouseRequest request) {
        log.info("Добавление нового товара: {}", request);

        if (warehouseProductRepository.existsById(request.getProductId())) {
            throw new SpecifiedProductAlreadyInWarehouseException("Товар уже зарегистрирован.");
        }

        ProductDto productDto = shoppingStoreClient.getProduct(request.getProductId());
        if (productDto == null) {
            throw new RuntimeException("Продукт с указанным ID отсутствует в магазине.");
        }

        WarehouseProduct newProduct = warehouseMapper.toWarehouseProduct(request);
        newProduct.setQuantityAvailable(0);

        return warehouseProductRepository.save(newProduct);
    }

    @Override
    public WarehouseProduct addProductQuantity(AddProductToWarehouseRequest request) {
        log.info("Добавление количества товара: {}", request);

        WarehouseProduct product = warehouseProductRepository.findById(request.getProductId())
                .orElseThrow(() -> new NoSpecifiedProductInWarehouseException("Товар не найден на складе."));

        product.setQuantityAvailable(product.getQuantityAvailable() + request.getQuantity());
        return warehouseProductRepository.save(product);
    }

    @Override
    public void acceptReturn(Map<UUID, Integer> products) {
        log.info("Принятие возврата товаров: {}", products);

        products.forEach((productId, quantity) -> {
            WarehouseProduct product = warehouseProductRepository.findById(productId)
                    .orElseThrow(() -> new NoSpecifiedProductInWarehouseException("Товар не найден на складе."));
            product.setQuantityAvailable(product.getQuantityAvailable() + quantity);
            warehouseProductRepository.save(product);
        });
    }

    @Override
    public BookedProductsDto bookProducts(ShoppingCartDto shoppingCart) {
        log.info("Бронирование товаров по корзине: {}", shoppingCart);

        double totalWeight = 0;
        double totalVolume = 0;
        boolean fragile = false;

        for (Map.Entry<UUID, Integer> entry : shoppingCart.getProducts().entrySet()) {
            UUID productId = entry.getKey();
            int requestedQuantity = entry.getValue();

            WarehouseProduct product = warehouseProductRepository.findById(productId)
                    .orElseThrow(() -> new NoSpecifiedProductInWarehouseException("Товар не найден: " + productId));

            if (product.getQuantityAvailable() < requestedQuantity) {
                throw new ProductInShoppingCartLowQuantityInWarehouse(
                        "Недостаточное количество товара: " + productId);
            }

            totalWeight += product.getWeight() * requestedQuantity;
            totalVolume += product.getDimension().getWidth()
                    * product.getDimension().getHeight()
                    * product.getDimension().getDepth() * requestedQuantity;

            fragile |= product.isFragile();
        }

        return BookedProductsDto.builder()
                .deliveryWeight(totalWeight)
                .deliveryVolume(totalVolume)
                .fragile(fragile)
                .build();
    }

    @Override
    public BookedProductsDto assembleOrder(AssemblyProductForOrderFromShoppingCartRequest request) {
        log.info("Сборка заказа для корзины с ID: {} и заказа с ID: {}", request.getShoppingCartId(), request.getOrderId());

        ShoppingCartDto cartDto = shoppingCartClient.getShoppingCartById(request.getShoppingCartId());
        if (cartDto == null || cartDto.getProducts().isEmpty()) {
            throw new NoSpecifiedProductInWarehouseException("Корзина пуста или не найдена.");
        }

        double totalWeight = 0;
        double totalVolume = 0;
        boolean fragile = false;

        for (Map.Entry<UUID, Integer> entry : cartDto.getProducts().entrySet()) {
            UUID productId = entry.getKey();
            int quantity = entry.getValue();

            WarehouseProduct product = warehouseProductRepository.findById(productId)
                    .orElseThrow(() -> new NoSpecifiedProductInWarehouseException("Товар с ID " + productId + " не найден на складе."));
            if (product.getQuantityAvailable() < quantity) {
                throw new NoSpecifiedProductInWarehouseException("Недостаточно товара с ID " + productId + " для сборки.");
            }

            product.setQuantityAvailable(product.getQuantityAvailable() - quantity);
            warehouseProductRepository.save(product);

            totalWeight += product.getWeight() * quantity;
            totalVolume += product.getDimension().getWidth()
                    * product.getDimension().getHeight()
                    * product.getDimension().getDepth() * quantity;
            fragile |= product.isFragile();
        }

        return BookedProductsDto.builder()
                .deliveryWeight(totalWeight)
                .deliveryVolume(totalVolume)
                .fragile(fragile)
                .build();
    }

    @Override
    public AddressDto getWarehouseAddress() {
        log.info("Получение адреса склада");

        return AddressDto.builder()
                .country("Deutschland")
                .city("Hamburg")
                .street("Warehouse Street")
                .house("10")
                .flat("1")
                .build();
    }
}
