package ru.yandex.practicum.shoppingStore.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PageableDto {

    @NotNull(message = "Номер страницы не должен быть пустым")
    @Min(value = 0, message = "Номер страницы должен быть >= 0")
    private int page;

    @NotNull(message = "Размер страницы не должен быть пустым")
    @Min(value = 1, message = "Размер страницы должен быть >= 1")
    private int size;

    private List<String> sort;
}
