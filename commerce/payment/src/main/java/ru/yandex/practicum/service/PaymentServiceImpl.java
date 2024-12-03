package ru.yandex.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.exception.PaymentNotFoundException;
import ru.yandex.practicum.mapper.PaymentMapper;
import ru.yandex.practicum.modul.Payment;
import ru.yandex.practicum.order.dto.OrderDto;
import ru.yandex.practicum.payment.dto.PaymentDto;
import ru.yandex.practicum.payment.enums.PaymentState;
import ru.yandex.practicum.repository.PaymentRepository;
import ru.yandex.practicum.shoppingStore.feign.ShoppingStoreClient;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentMapper paymentMapper;
    private final ShoppingStoreClient shoppingStoreClient;

    @Override
    public BigDecimal productCost(OrderDto orderDto) {
        log.info("Расчет стоимости товаров для заказа: {}", orderDto.getOrderId());

        BigDecimal total = BigDecimal.ZERO;

        for (Map.Entry<UUID, Integer> entry : orderDto.getProducts().entrySet()) {
            UUID productId = entry.getKey();
            Integer quantity = entry.getValue();

            BigDecimal productPrice = shoppingStoreClient.getProduct(productId).getPrice();

            BigDecimal lineTotal = productPrice.multiply(BigDecimal.valueOf(quantity));
            total = total.add(lineTotal);
        }

        log.info("Стоимость товаров для заказа {}: {}", orderDto.getOrderId(), total);
        return total;
    }

    @Override
    public BigDecimal getTotalCost(OrderDto orderDto) {
        log.info("Расчет общей стоимости для заказа: {}", orderDto.getOrderId());

        BigDecimal productTotal = productCost(orderDto);
        BigDecimal deliveryPrice = orderDto.getDeliveryPrice();

        BigDecimal vat = productTotal.multiply(BigDecimal.valueOf(0.1));

        BigDecimal total = productTotal.add(vat).add(deliveryPrice);

        log.info("Общая стоимость для заказа {}: {}", orderDto.getOrderId(), total);
        return total;
    }

    @Override
    @Transactional
    public PaymentDto payment(OrderDto orderDto) {
        log.info("Создание платежа для заказа: {}", orderDto.getOrderId());

        BigDecimal productTotal = productCost(orderDto);

        BigDecimal deliveryTotal = orderDto.getDeliveryPrice();

        BigDecimal totalPayment = getTotalCost(orderDto);

        Payment payment = Payment.builder()
                .orderId(orderDto.getOrderId())
                .productTotal(productTotal)
                .deliveryTotal(deliveryTotal)
                .totalPayment(totalPayment)
                .state(PaymentState.PENDING)
                .build();

        paymentRepository.save(payment);

        log.info("Платеж с ID {} создан для заказа {}", payment.getPaymentId(), orderDto.getOrderId());

        return paymentMapper.toPaymentDto(payment);
    }

    @Override
    @Transactional
    public void paymentSuccess(UUID paymentId) {
        log.info("Обработка успешной оплаты для платежа: {}", paymentId);

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentNotFoundException("Платеж не найден: " + paymentId));

        payment.setState(PaymentState.SUCCESS);
        paymentRepository.save(payment);
    }

    @Override
    @Transactional
    public void paymentFailed(UUID paymentId) {
        log.info("Обработка отказа в оплате для платежа: {}", paymentId);

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentNotFoundException("Платеж не найден: " + paymentId));

        payment.setState(PaymentState.FAILED);
        paymentRepository.save(payment);
    }
}
