package ru.yandex.practicum.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.service.ShoppingStoreService;
import ru.yandex.practicum.shoppingStore.dto.PageableDto;
import ru.yandex.practicum.shoppingStore.dto.ProductDto;
import ru.yandex.practicum.shoppingStore.dto.SetProductQuantityStateRequest;
import ru.yandex.practicum.shoppingStore.enums.ProductCategory;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class ShoppingStoreController {

    private final ShoppingStoreService shoppingStoreService;

    @GetMapping
    List<ProductDto> getProducts(@RequestParam("category") ProductCategory category,
                                 @Valid @ModelAttribute PageableDto pageableDto) {
        return shoppingStoreService.getProductsByCategory(category, pageableDto);
    }

    @PutMapping
    ProductDto createNewProduct(@Valid @RequestBody ProductDto productDto) {
        return shoppingStoreService.createNewProduct(productDto);
    }

    @PostMapping
    ProductDto updateProduct(@RequestBody ProductDto productDto)  {
        return shoppingStoreService.updateProduct(productDto);
    }

    @PostMapping("/removeProductFromStore")
    boolean removeProductFromStore(@RequestParam("productId") UUID productId) {
        return shoppingStoreService.removeProductFromStore(productId);
    }

    @PostMapping("/quantityState")
    boolean setProductQuantityState(@Valid @RequestBody SetProductQuantityStateRequest request){
        return shoppingStoreService.setProductQuantityState(request);
    }

    @GetMapping("/{productId}")
    ProductDto getProduct(@PathVariable("productId") UUID productId)  {
        return shoppingStoreService.getProduct(productId);
    }
}
