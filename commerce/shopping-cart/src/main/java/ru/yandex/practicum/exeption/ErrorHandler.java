package ru.yandex.practicum.exeption;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@RestControllerAdvice
@Slf4j
public class ErrorHandler {

    @ExceptionHandler(NoProductsInShoppingCartException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleNotAuthorizedUserException(NoProductsInShoppingCartException ex) {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, "Корзина пуста", ex);
    }

    @ExceptionHandler(NotAuthorizedUserException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ErrorResponse handleNotAuthorizedUserException(NotAuthorizedUserException ex) {
        return buildErrorResponse(HttpStatus.UNAUTHORIZED, "Неавторизованный пользователь", ex);
    }

    @ExceptionHandler(ProductNotAvailableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleProductNotAvailableException(ProductNotAvailableException ex) {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, "Товар недоступен", ex);
    }

    @ExceptionHandler(InternalServerErrorException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleInternalServerErrorException(InternalServerErrorException ex) {
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Внутренняя ошибка сервера", ex);
    }

    private ErrorResponse buildErrorResponse(HttpStatus status, String userMessage, Throwable ex) {
        log.error("Ошибка: {}", ex.getMessage(), ex);

        return ErrorResponse.builder()
                .cause(ex.getCause())
                .stackTrace(List.of(ex.getStackTrace()))
                .httpStatus(status.name())
                .userMessage(userMessage)
                .message(ex.getMessage())
                .suppressed(List.of(ex.getSuppressed()))
                .localizedMessage(ex.getLocalizedMessage())
                .build();
    }
}
