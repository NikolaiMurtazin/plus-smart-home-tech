package ru.yandex.practicum.service;

import ru.yandex.practicum.delivery.dto.DeliveryDto;
import ru.yandex.practicum.order.dto.OrderDto;

import java.math.BigDecimal;
import java.util.UUID;

public interface DeliveryService {
    DeliveryDto planDelivery(DeliveryDto deliveryDto);

    void deliverySuccessful(UUID orderId);

    void deliveryPicked(UUID deliveryId);

    void deliveryFailed(UUID orderId);

    BigDecimal deliveryCost(OrderDto orderDto);
}
