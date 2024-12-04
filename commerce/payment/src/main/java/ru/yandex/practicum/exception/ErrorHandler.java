package ru.yandex.practicum.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@RestControllerAdvice
@Slf4j
public class ErrorHandler {

    @ExceptionHandler(NotEnoughInfoInOrderToCalculateException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleNotEnoughInfoInOrderToCalculateException(NotEnoughInfoInOrderToCalculateException ex) {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, "Недостаточно информации в заказе для расчёта", ex);
    }

    @ExceptionHandler(PaymentNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handlePaymentNotFoundException(PaymentNotFoundException ex) {
        return buildErrorResponse(HttpStatus.NOT_FOUND, "Платеж не найден", ex);
    }

    @ExceptionHandler(NoOrderFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleNoOrderFoundException(NoOrderFoundException ex) {
        return buildErrorResponse(HttpStatus.NOT_FOUND, "Заказ не найден", ex);
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
