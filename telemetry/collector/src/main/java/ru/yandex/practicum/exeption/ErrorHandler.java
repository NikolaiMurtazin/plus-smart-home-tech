package ru.yandex.practicum.exeption;

import net.devh.boot.grpc.server.advice.GrpcAdvice;
import net.devh.boot.grpc.server.advice.GrpcExceptionHandler;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@GrpcAdvice
public class ErrorHandler {
    @GrpcExceptionHandler({MethodArgumentNotValidException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleMethodArgumentNotValidException(final MethodArgumentNotValidException ex) {
        return new ApiError(
                HttpStatus.BAD_REQUEST.name(),
                "Incorrectly made request.",
                ex.getMessage() + extracted(ex),
                LocalDateTime.now()
        );
    }

    @GrpcExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError handleNotFoundException(final NotFoundException ex) {
        return new ApiError(
                HttpStatus.NOT_FOUND.name(),
                "The required object was not found.",
                ex.getMessage() + extracted(ex),
                LocalDateTime.now()
        );
    }

    @GrpcExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiError handleThrowable(final Throwable ex) {
        List<String> errors = Collections.singletonList(ex.getMessage());

        return new ApiError(
                HttpStatus.INTERNAL_SERVER_ERROR.name(),
                "An unexpected error occurred.",
                ex.getMessage() + extracted(ex),
                errors,
                LocalDateTime.now()
        );
    }

    private static String extracted(Throwable ex) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        ex.printStackTrace(pw);
        return sw.toString();
    }
}
