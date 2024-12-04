package ru.yandex.practicum.mapper;

import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import ru.yandex.practicum.model.ShoppingCart;
import ru.yandex.practicum.shoppingCart.dto.ShoppingCartDto;


@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface ShoppingCartMapper {
    ShoppingCartDto toShoppingCartDto(final ShoppingCart product);
}
