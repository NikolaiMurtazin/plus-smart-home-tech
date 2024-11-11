package ru.yandex.practicum.service;

public class DeserializationException extends RuntimeException {
    public DeserializationException(String message, Throwable cause) {
        super(message, cause);
    }
}
