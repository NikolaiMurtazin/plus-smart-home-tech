package ru.yandex.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.delivery.dto.AddressDto;
import ru.yandex.practicum.exception.NoSpecifiedProductInWarehouseException;
import ru.yandex.practicum.exception.ProductInShoppingCartLowQuantityInWarehouse;
import ru.yandex.practicum.exception.SpecifiedProductAlreadyInWarehouseException;
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
import ru.yandex.practicum.warehouse.dto.AssemblyProductForOrderFromShoppingCartRequest;
import ru.yandex.practicum.warehouse.dto.BookedProductDto;
import ru.yandex.practicum.warehouse.dto.NewProductInWarehouseRequest;
import ru.yandex.practicum.warehouse.dto.ShippedToDeliveryRequest;

import java.security.SecureRandom;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class WarehouseServiceImpl implements WarehouseService {

    private static final String[] ADDRESSES = new String[] {"ADDRESS_1", "ADDRESS_2"};
    private static final String CURRENT_ADDRESS = ADDRESSES[Random.from(new SecureRandom()).nextInt(0, 1)];

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
    public void shippedToDelivery(ShippedToDeliveryRequest request) {
        log.info("Передача товаров в доставку для заказа с ID: {} и доставки с ID: {}", request.getOrderId(), request.getDeliveryId());

        Booking booking = bookingRepository.findByOrderId(request.getOrderId())
                .orElseThrow(() -> new NoSpecifiedProductInWarehouseException("Бронирование с orderId " + request.getOrderId() + " не найдено."));

        booking.setDeliveryId(request.getDeliveryId());

        bookingRepository.save(booking);

        log.info("Товары для заказа с ID: {} успешно переданы в доставку с ID: {}", request.getOrderId(), request.getDeliveryId());
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

            QuantityState newState = QuantityState.determineState(product.getQuantityAvailable());
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

    @Override
    @Transactional
    public BookedProductDto assemblyProductForOrderFromShoppingCart(AssemblyProductForOrderFromShoppingCartRequest request) {
        log.info("Сборка заказа для корзины с ID: {} и заказа с ID: {}", request.getShoppingCartId(), request.getOrderId());

        Booking booking = bookingRepository.findById(request.getShoppingCartId())
                .orElseThrow(() -> new NoSpecifiedProductInWarehouseException("Бронирование с ID " + request.getShoppingCartId() + " не найдено."));

        if (booking.getProducts() == null || booking.getProducts().isEmpty()) {
            throw new NoSpecifiedProductInWarehouseException("Корзина пуста или не найдена.");
        }

        booking.setOrderId(request.getOrderId());
        bookingRepository.save(booking);

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

        QuantityState newState = QuantityState.determineState(updatedProduct.getQuantityAvailable());
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

        AddressDto addressDto = new AddressDto();
        addressDto.setStreet(CURRENT_ADDRESS);

        return addressDto;
    }
}
