package ru.yandex.practicum.mapper;

import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import ru.yandex.practicum.model.Dimension;
import ru.yandex.practicum.warehouse.dto.DimensionDto;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        injectionStrategy = InjectionStrategy.CONSTRUCTOR
)
public interface DimensionMapper {
    Dimension toDimension(DimensionDto dto);
    DimensionDto toDimensionDto(Dimension dimension);
}
