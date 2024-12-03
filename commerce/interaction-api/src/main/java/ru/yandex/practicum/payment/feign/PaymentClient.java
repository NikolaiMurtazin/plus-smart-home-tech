package ru.yandex.practicum.payment.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import ru.yandex.practicum.order.dto.OrderDto;
import ru.yandex.practicum.payment.dto.PaymentDto;

import java.math.BigDecimal;
import java.util.UUID;

@FeignClient(name = "payment-service", path = "/api/v1/payment")
public interface PaymentClient {

    @PostMapping("/productCost")
    BigDecimal productCost(@RequestBody OrderDto orderDto); // order

    @PostMapping("/totalCost")
    BigDecimal getTotalCost(@RequestBody OrderDto orderDto); // order

    @PostMapping
    PaymentDto payment(@RequestBody OrderDto orderDto); // order

    @PostMapping("/api/v1/order/payment")
    void paymentSuccess(@RequestParam UUID paymentId); // order

    @PostMapping("/api/v1/order/payment/failed")
    void paymentFailed(@RequestParam UUID paymentId); // order

}
