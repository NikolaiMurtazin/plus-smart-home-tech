package ru.yandex.practicum.consumer;

public class DeserializationException extends RuntimeException {
    public DeserializationException(String message, Throwable cause) {
        super(message, cause);
    }
}
