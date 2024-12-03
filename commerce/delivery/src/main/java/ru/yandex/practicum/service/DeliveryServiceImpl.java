package ru.yandex.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.delivery.dto.DeliveryDto;
import ru.yandex.practicum.delivery.enums.DeliveryState;
import ru.yandex.practicum.exception.NoDeliveryFoundException;
import ru.yandex.practicum.exception.NoOrderFoundException;
import ru.yandex.practicum.mapper.DeliveryMapper;
import ru.yandex.practicum.model.Delivery;
import ru.yandex.practicum.order.dto.OrderDto;
import ru.yandex.practicum.order.feign.OrderClient;
import ru.yandex.practicum.repository.DeliveryRepository;
import ru.yandex.practicum.warehouse.dto.ShippedToDeliveryRequest;
import ru.yandex.practicum.warehouse.feign.WarehouseClient;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class DeliveryServiceImpl implements DeliveryService {

    private final DeliveryRepository deliveryRepository;
    private final DeliveryMapper deliveryMapper;
    private final OrderClient orderClient;
    private final WarehouseClient warehouseClient;


    @Override
    @Transactional
    public DeliveryDto planDelivery(DeliveryDto deliveryDto) {
        log.info("Планирование доставки: {}", deliveryDto);

        OrderDto orderDto = orderClient.delivery(deliveryDto.getOrderId());

        deliveryDto.setState(DeliveryState.CREATED);

        Delivery delivery = deliveryMapper.fromDeliveryDto(deliveryDto);
        delivery.setDeliveryWeight(orderDto.getDeliveryWeight());
        delivery.setDeliveryVolume(orderDto.getDeliveryVolume());
        delivery.setFragile(orderDto.isFragile());

        deliveryRepository.save(delivery);

        log.info("Доставка с ID {} запланирована", delivery.getDeliveryId());
        return deliveryMapper.toDeliveryDto(delivery);
    }

    @Override
    public void deliverySuccessful(UUID orderId) {
        log.info("Обработка успешной доставки для заказа: {}", orderId);

        Delivery delivery = deliveryRepository.findByOrderId(orderId)
                .orElseThrow(() -> new NoOrderFoundException("Не найден заказ: " + orderId));

        delivery.setState(DeliveryState.DELIVERED);
        deliveryRepository.save(delivery);
    }

    @Override
    @Transactional
    public void deliveryPicked(UUID deliveryId) {
        log.info("Получение товара для доставки: {}", deliveryId);

        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new NoDeliveryFoundException("Доставка не найдена: " + deliveryId));

        delivery.setState(DeliveryState.IN_DELIVERY);
        deliveryRepository.save(delivery);

        ShippedToDeliveryRequest request = ShippedToDeliveryRequest.builder()
                .orderId(delivery.getOrderId())
                .deliveryId(deliveryId)
                .build();

        warehouseClient.shippedToDelivery(request);
    }

    @Override
    @Transactional
    public void deliveryFailed(UUID orderId) {
        log.info("Обработка неудачной доставки для заказа: {}", orderId);

        Delivery delivery = deliveryRepository.findByOrderId(orderId)
                .orElseThrow(() -> new NoOrderFoundException("Не найден заказ: " + orderId));
        delivery.setState(DeliveryState.FAILED);
        deliveryRepository.save(delivery);

        orderClient.deliveryFailed(orderId);
    }

    @Override
    public BigDecimal deliveryCost(OrderDto orderDto) {

        Delivery delivery = deliveryRepository.findById(orderDto.getDeliveryId())
                .orElseThrow(() -> new NoDeliveryFoundException("Не найдена доставка для расчёта: " + orderDto.getDeliveryId()));

        String warehouseAddress = String.valueOf(warehouseClient.getWarehouseAddress());

        BigDecimal baseRate = BigDecimal.valueOf(5.0);

        BigDecimal warehouseMultiplier = warehouseAddress.contains("ADDRESS_1") ? BigDecimal.ONE : BigDecimal.valueOf(2);
        BigDecimal step1 = baseRate.multiply(warehouseMultiplier).add(baseRate);

        BigDecimal fragileAddition = orderDto.isFragile() ? step1.multiply(BigDecimal.valueOf(0.2)) : BigDecimal.ZERO;
        BigDecimal step2 = step1.add(fragileAddition);

        BigDecimal weightAddition = BigDecimal.valueOf(orderDto.getDeliveryWeight()).multiply(BigDecimal.valueOf(0.3));
        BigDecimal step3 = step2.add(weightAddition);

        BigDecimal volumeAddition = BigDecimal.valueOf(orderDto.getDeliveryVolume()).multiply(BigDecimal.valueOf(0.2));
        BigDecimal step4 = step3.add(volumeAddition);

        String deliveryStreet = delivery.getToAddress().getStreet();
        BigDecimal addressAddition = warehouseAddress.equals(deliveryStreet)
                ? BigDecimal.ZERO
                : step4.multiply(BigDecimal.valueOf(0.2));
        BigDecimal totalCost = step4.add(addressAddition);

        log.info("Стоимость доставки для заказа {}: {}", orderDto.getOrderId(), totalCost);
        return totalCost;
    }
}
