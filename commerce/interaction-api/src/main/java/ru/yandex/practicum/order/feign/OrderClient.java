package ru.yandex.practicum.order.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.yandex.practicum.order.dto.OrderDto;

import java.util.UUID;

@FeignClient(name = "order-service", path = "/api/v1/order")
public interface OrderClient {

    @PostMapping("/delivery")
    OrderDto delivery(@RequestParam UUID orderId);

    @PostMapping("/delivery/failed")
    OrderDto deliveryFailed(@RequestParam UUID orderId);

}
