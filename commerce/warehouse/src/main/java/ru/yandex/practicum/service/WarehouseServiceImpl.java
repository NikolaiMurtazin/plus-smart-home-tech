package ru.yandex.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.exeption.NoSpecifiedProductInWarehouseException;
import ru.yandex.practicum.exeption.ProductInShoppingCartLowQuantityInWarehouse;
import ru.yandex.practicum.exeption.SpecifiedProductAlreadyInWarehouseException;
import ru.yandex.practicum.mapper.BookingMapper;
import ru.yandex.practicum.mapper.WarehouseMapper;
import ru.yandex.practicum.model.Booking;
import ru.yandex.practicum.model.WarehouseProduct;
import ru.yandex.practicum.repository.BookingRepository;
import ru.yandex.practicum.repository.WarehouseProductRepository;
import ru.yandex.practicum.shoppingCart.dto.ShoppingCartDto;
import ru.yandex.practicum.shoppingStore.dto.ProductDto;
import ru.yandex.practicum.shoppingStore.dto.SetProductQuantityStateRequest;
import ru.yandex.practicum.shoppingStore.enums.QuantityState;
import ru.yandex.practicum.shoppingStore.feign.ShoppingStoreClient;
import ru.yandex.practicum.warehouse.dto.AddProductToWarehouseRequest;
import ru.yandex.practicum.warehouse.dto.AddressDto;
import ru.yandex.practicum.warehouse.dto.AssemblyProductForOrderFromShoppingCartRequest;
import ru.yandex.practicum.warehouse.dto.BookedProductDto;
import ru.yandex.practicum.warehouse.dto.NewProductInWarehouseRequest;

import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class WarehouseServiceImpl implements WarehouseService {

    private final WarehouseProductRepository warehouseProductRepository;
    private final BookingRepository bookingRepository;
    private final ShoppingStoreClient shoppingStoreClient;
    private final WarehouseMapper warehouseMapper;
    private final BookingMapper bookingMapper;

    @Override
    public void addNewProduct(NewProductInWarehouseRequest request) {
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

        warehouseProductRepository.save(newProduct);
    }

    @Override
    @Transactional
    public void acceptReturn(Map<UUID, Integer> products) {
        log.info("Принятие возврата товаров: {}", products);

        products.forEach((productId, quantity) -> {
            WarehouseProduct product = warehouseProductRepository.findById(productId)
                    .orElseThrow(() -> new NoSpecifiedProductInWarehouseException("Товар не найден на складе."));
            product.setQuantityAvailable(product.getQuantityAvailable() + quantity);
            warehouseProductRepository.save(product);
        });
    }

    // todo нужно не забыть потом удалять их после отправки с бд
    @Override
    @Transactional
    public BookedProductDto bookProductForShoppingCart(ShoppingCartDto shoppingCart) {
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
                throw new ProductInShoppingCartLowQuantityInWarehouse("Недостаточное количество товара: " + productId);
            }

            product.setQuantityAvailable(product.getQuantityAvailable() - requestedQuantity);
            warehouseProductRepository.save(product);

            QuantityState newState = determineState(product.getQuantityAvailable());
            shoppingStoreClient.setProductQuantityState(
                    SetProductQuantityStateRequest.builder()
                            .productId(productId)
                            .quantityState(newState)
                            .build()
            );

            totalWeight += product.getWeight() * requestedQuantity;
            totalVolume += product.getDimension().getWidth()
                    * product.getDimension().getHeight()
                    * product.getDimension().getDepth() * requestedQuantity;

            fragile |= product.isFragile();
        }

        Booking booking = Booking.builder()
                .shoppingCartId(shoppingCart.getShoppingCartId())
                .products(shoppingCart.getProducts())
                .deliveryWeight(totalWeight)
                .deliveryVolume(totalVolume)
                .fragile(fragile)
                .build();

        bookingRepository.save(booking);

        log.info("Сборка заказа завершена для корзины с ID: {}", shoppingCart.getShoppingCartId());

        return bookingMapper.toBookedProductDto(booking);
    }

    // todo зачем нужен orderId узнаю походу в следующем спринте )))
    @Override
    @Transactional(readOnly = true)
    public BookedProductDto assemblyProductForOrderFromShoppingCart(AssemblyProductForOrderFromShoppingCartRequest request) {
        log.info("Сборка заказа для корзины с ID: {} и заказа с ID: {}", request.getShoppingCartId(), request.getOrderId());

        Booking booking = bookingRepository.findById(request.getShoppingCartId())
                .orElseThrow(() -> new NoSpecifiedProductInWarehouseException("Бронирование с ID " + request.getShoppingCartId() + " не найдено."));

        if (booking.getProducts() == null || booking.getProducts().isEmpty()) {
            throw new NoSpecifiedProductInWarehouseException("Корзина пуста или не найдена.");
        }

        log.info("Сборка заказа завершена для корзины с ID: {} и заказа с ID: {}", request.getShoppingCartId(), request.getOrderId());

        return bookingMapper.toBookedProductDto(booking);
    }

    @Override
    @Transactional
    public void addProductQuantity(AddProductToWarehouseRequest request) {
        log.info("Добавление количества товара: {}", request);

        WarehouseProduct product = warehouseProductRepository.findById(request.getProductId())
                .orElseThrow(() -> new NoSpecifiedProductInWarehouseException("Товар не найден на складе."));

        product.setQuantityAvailable(product.getQuantityAvailable() + request.getQuantity());
        WarehouseProduct updatedProduct = warehouseProductRepository.save(product);

        QuantityState newState = determineState(updatedProduct.getQuantityAvailable());
        shoppingStoreClient.setProductQuantityState(
                SetProductQuantityStateRequest.builder()
                        .productId(request.getProductId())
                        .quantityState(newState)
                        .build()
        );
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

    private QuantityState determineState(int quantity) {
        if (quantity == 0) {
            return QuantityState.ENDED;
        } else if (quantity > 0 && quantity < 5) {
            return QuantityState.FEW;
        } else if (quantity >= 5 && quantity <= 20) {
            return QuantityState.ENOUGH;
        } else {
            return QuantityState.MANY;
        }
    }
}
