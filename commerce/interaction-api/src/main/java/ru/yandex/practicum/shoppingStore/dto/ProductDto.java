package ru.yandex.practicum.shoppingStore.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.yandex.practicum.shoppingStore.enums.ProductCategory;
import ru.yandex.practicum.shoppingStore.enums.ProductState;
import ru.yandex.practicum.shoppingStore.enums.QuantityState;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProductDto {
    private UUID productId;

    @NotBlank(message = "Наименование товара не должно быть пустым")
    private String productName;
    @NotBlank(message = "Описание товара не должно быть пустым")
    private String description;

    private String imageSrc;

    @NotNull(message = "Статус количества не должен быть пустым")
    private QuantityState quantityState;

    @NotNull(message = "Статус товара не должен быть пустым")
    private ProductState productState;

    @DecimalMin(value = "1.0", message = "Рейтинг товара должен быть не меньше 1")
    @DecimalMax(value = "5.0", message = "Рейтинг товара не может превышать 5")
    private double rating;

    @NotNull(message = "Категория товара не должна быть пустой")
    private ProductCategory productCategory;

    @DecimalMin(value = "1.0", message = "Цена товара должна быть не меньше 1")
    private BigDecimal price;
}
