package ru.yandex.practicum.config;

import feign.Response;
import feign.codec.ErrorDecoder;
import jakarta.ws.rs.BadRequestException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.exeption.InternalServerErrorException;
import ru.yandex.practicum.exeption.ProductNotFoundException;


@Component
@Slf4j
public class CustomErrorDecoder implements ErrorDecoder {

    private final ErrorDecoder defaultErrorDecoder = new Default();

    @Override
    public Exception decode(String methodKey, Response response) {
        HttpStatus httpStatus = HttpStatus.resolve(response.status());
        log.error("Ошибка Feign: метод {}, статус {}, причина {}", methodKey, response.status(), response.reason());

        assert httpStatus != null;
        return switch (httpStatus) {
            case NOT_FOUND -> new ProductNotFoundException("Продукт не найден");
            case BAD_REQUEST -> new BadRequestException("Некорректный запрос");
            case INTERNAL_SERVER_ERROR -> new InternalServerErrorException("Внутренняя ошибка сервера");
            default -> defaultErrorDecoder.decode(methodKey, response);
        };
    }
}
