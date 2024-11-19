package ru.yandex.practicum.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.model.WarehouseProduct;
import ru.yandex.practicum.service.WarehouseServiceImpl;
import ru.yandex.practicum.shoppingCart.dto.ShoppingCartDto;
import ru.yandex.practicum.warehouse.dto.AddProductToWarehouseRequest;
import ru.yandex.practicum.warehouse.dto.AddressDto;
import ru.yandex.practicum.warehouse.dto.AssemblyProductForOrderFromShoppingCartRequest;
import ru.yandex.practicum.warehouse.dto.BookedProductsDto;
import ru.yandex.practicum.warehouse.dto.NewProductInWarehouseRequest;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/warehouse")
@RequiredArgsConstructor
public class WarehouseController {

    private final WarehouseServiceImpl warehouseService;

    @PutMapping
    public WarehouseProduct addNewProduct(@RequestBody @Valid NewProductInWarehouseRequest request) {
        return warehouseService.addNewProduct(request);
    }

    @PostMapping("/add")
    public WarehouseProduct addProductQuantity(@RequestBody @Valid AddProductToWarehouseRequest request) {
        return warehouseService.addProductQuantity(request);
    }

    @PostMapping("/return")
    public void acceptReturn(@RequestBody Map<UUID, Integer> products) {
        warehouseService.acceptReturn(products);
    }

    @PostMapping("/booking")
    public BookedProductsDto bookProducts(@RequestBody @Valid ShoppingCartDto cartDto) {
        return warehouseService.bookProducts(cartDto);
    }

    @PostMapping("/assembly")
    public BookedProductsDto assembleOrder(
            @RequestBody @Valid AssemblyProductForOrderFromShoppingCartRequest request) {
        return warehouseService.assembleOrder(request);
    }

    @GetMapping("/address")
    public AddressDto getWarehouseAddress() {
        return warehouseService.getWarehouseAddress();
    }
}
