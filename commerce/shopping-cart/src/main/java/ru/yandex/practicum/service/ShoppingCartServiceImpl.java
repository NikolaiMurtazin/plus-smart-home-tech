package ru.yandex.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.exeption.NoProductsInShoppingCartException;
import ru.yandex.practicum.exeption.NotAuthorizedUserException;
import ru.yandex.practicum.mapper.CartMapper;
import ru.yandex.practicum.model.ShoppingCart;
import ru.yandex.practicum.repository.ShoppingCartRepository;
import ru.yandex.practicum.shoppingCart.dto.ChangeProductQuantityRequest;
import ru.yandex.practicum.shoppingCart.dto.ShoppingCartDto;
import ru.yandex.practicum.warehouse.dto.BookedProductDto;
import ru.yandex.practicum.warehouse.feign.WarehouseClient;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ShoppingCartServiceImpl implements ShoppingCartService {

    private final ShoppingCartRepository shoppingCartRepository;
    private final WarehouseClient warehouseClient;
    private final CartMapper cartMapper;

    @Override
    public ShoppingCartDto getShoppingCart(String username) {
        log.info("Получение корзины для пользователя: {}", username);
        validateUsername(username);

        ShoppingCart shoppingCart = shoppingCartRepository.findByUsernameAndActive(username, true)
                .orElseGet(() -> createNewShoppingCart(username));

        ShoppingCartDto shoppingCartDto = cartMapper.toShoppingCartDto(shoppingCart);
        log.info("Корзина пользователя {} успешно получена: {}", username, shoppingCartDto);
        return shoppingCartDto;
    }

    @Override
    @Transactional
    public ShoppingCartDto addProducts(String username, Map<UUID, Integer> products) {
        log.info("Добавление товаров в корзину для пользователя: {}", username);
        validateUsername(username);

        ShoppingCart shoppingCart = getActiveShoppingCart(username);

        for (Map.Entry<UUID, Integer> entry : products.entrySet()) {
            UUID productId = entry.getKey();
            int quantity = entry.getValue();

            updateProductQuantity(shoppingCart, productId, quantity);
        }

        shoppingCartRepository.save(shoppingCart);
        ShoppingCartDto shoppingCartDto = cartMapper.toShoppingCartDto(shoppingCart);
        log.info("Корзина пользователя {} успешно обновлена: {}", username, shoppingCartDto);
        return shoppingCartDto;
    }

    @Override
    public void deactivateShoppingCart(String username) {
        log.info("Деактивация корзины для пользователя: {}", username);
        validateUsername(username);

        ShoppingCart shoppingCart = getActiveShoppingCart(username);

        shoppingCart.setActive(false);
        shoppingCartRepository.save(shoppingCart);
        log.info("Корзина пользователя {} успешно деактивирована.", username);
    }

    @Override
    @Transactional
    public ShoppingCartDto removeProducts(String username, Map<UUID, Integer> products) {
        log.info("Удаление товаров из корзины для пользователя: {}", username);
        validateUsername(username);

        ShoppingCart shoppingCart = getActiveShoppingCart(username);

        for (Map.Entry<UUID, Integer> entry : products.entrySet()) {
            UUID productId = entry.getKey();
            int quantityToRemove = entry.getValue();
            updateProductQuantity(shoppingCart, productId, -quantityToRemove);
        }

        shoppingCartRepository.save(shoppingCart);
        ShoppingCartDto shoppingCartDto = cartMapper.toShoppingCartDto(shoppingCart);
        log.info("Корзина пользователя {} успешно обновлена: {}", username, shoppingCartDto);
        return shoppingCartDto;
    }

    @Override
    @Transactional
    public ShoppingCartDto changeProductQuantity(String username, ChangeProductQuantityRequest request) {
        log.info("Изменение количества товара в корзине для пользователя: {}", username);
        validateUsername(username);

        ShoppingCart shoppingCart = getActiveShoppingCart(username);

        UUID productId = request.getProductId();
        int newQuantity = request.getNewQuantity();

        if (!shoppingCart.getProducts().containsKey(productId)) {
            throw new NoProductsInShoppingCartException("Товар не найден в корзине: " + productId);
        }

        shoppingCart.getProducts().put(productId, newQuantity);
        shoppingCartRepository.save(shoppingCart);

        ShoppingCartDto shoppingCartDto = cartMapper.toShoppingCartDto(shoppingCart);
        log.info("Количество товара {} в корзине пользователя {} успешно обновлено до {}", productId, username, newQuantity);
        return shoppingCartDto;
    }

    @Override
    @Transactional
    public BookedProductDto bookProducts(String username) {
        log.info("Бронирование товаров для пользователя: {}", username);
        validateUsername(username);

        ShoppingCart shoppingCart = getActiveShoppingCart(username);

        Map<UUID, Integer> products = shoppingCart.getProducts();
        if (products.isEmpty()) {
            throw new NoProductsInShoppingCartException("Корзина пуста для пользователя: " + username);
        }

        try {
            BookedProductDto bookedProducts = warehouseClient.bookProducts(cartMapper.toShoppingCartDto(shoppingCart));

            shoppingCart.setActive(false);
            shoppingCartRepository.save(shoppingCart);

            log.info("Бронирование товаров для пользователя {} успешно выполнено.", username);
            return bookedProducts;
        } catch (Exception e) {
            log.error("Ошибка бронирования товаров для пользователя {}: {}", username, e.getMessage(), e);
            throw new RuntimeException("Не удалось забронировать товары: " + e.getMessage(), e);
        }
    }

    private ShoppingCart getActiveShoppingCart(String username) {
        return shoppingCartRepository.findByUsernameAndActive(username, true)
                .orElseThrow(() -> new NoProductsInShoppingCartException("Корзина не найдена для пользователя: " + username));
    }

    private void updateProductQuantity(ShoppingCart shoppingCart, UUID productId, int quantityChange) {
        shoppingCart.getProducts().merge(productId, quantityChange, (current, change) -> {
            int updatedQuantity = current + change;
            return updatedQuantity > 0 ? updatedQuantity : null;
        });
    }

    private void validateUsername(String username) {
        if (username == null || username.isEmpty()) {
            throw new NotAuthorizedUserException("Имя пользователя не должно быть пустым");
        }
    }

    private ShoppingCart createNewShoppingCart(String username) {
        log.info("Создание новой корзины для пользователя: {}", username);
        ShoppingCart cart = ShoppingCart.builder()
                .username(username)
                .active(true)
                .products(new HashMap<>())
                .build();
        return shoppingCartRepository.save(cart);
    }
}