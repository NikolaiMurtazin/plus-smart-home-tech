package ru.yandex.practicum.exeption;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class NoSpecifiedProductInWarehouseException extends RuntimeException {
    public NoSpecifiedProductInWarehouseException(String message) {
        super(message);
    }
}