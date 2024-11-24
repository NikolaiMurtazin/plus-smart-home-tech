package ru.yandex.practicum.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.service.WarehouseServiceImpl;
import ru.yandex.practicum.shoppingCart.dto.ShoppingCartDto;
import ru.yandex.practicum.warehouse.dto.AddProductToWarehouseRequest;
import ru.yandex.practicum.warehouse.dto.AddressDto;
import ru.yandex.practicum.warehouse.dto.AssemblyProductForOrderFromShoppingCartRequest;
import ru.yandex.practicum.warehouse.dto.BookedProductDto;
import ru.yandex.practicum.warehouse.dto.NewProductInWarehouseRequest;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/warehouse")
@RequiredArgsConstructor
public class WarehouseController {

    private final WarehouseServiceImpl warehouseService;

    @PutMapping
    public void addNewProduct(@RequestBody @Valid NewProductInWarehouseRequest request) {
        warehouseService.addNewProduct(request);
    }

    @PostMapping("/return")
    public void acceptReturn(@RequestBody Map<UUID, Integer> products) {
        warehouseService.acceptReturn(products);
    }

    @PostMapping("/booking")
    public BookedProductDto bookProductForShoppingCart(@RequestBody @Valid ShoppingCartDto cartDto) {
        return warehouseService.bookProductForShoppingCart(cartDto);
    }

    @PostMapping("/assembly")
    public BookedProductDto assemblyProductForOrderFromShoppingCart(
            @RequestBody @Valid AssemblyProductForOrderFromShoppingCartRequest request) {
        return warehouseService.assemblyProductForOrderFromShoppingCart(request);
    }

    @PostMapping("/add")
    public void addProductQuantity(@RequestBody @Valid AddProductToWarehouseRequest request) {
        warehouseService.addProductQuantity(request);
    }

    @GetMapping("/address")
    public AddressDto getWarehouseAddress() {
        return warehouseService.getWarehouseAddress();
    }
}
