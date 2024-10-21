package ru.yandex.practicum.service;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;

import com.google.protobuf.Empty;
import net.devh.boot.grpc.server.service.GrpcService;
import ru.yandex.practicum.grpc.telemetry.collector.CollectorControllerGrpc;
import ru.yandex.practicum.grpc.telemetry.event.HubEventProto;
import ru.yandex.practicum.grpc.telemetry.event.SensorEventProto;

import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@GrpcService
public class EventCollectorService extends CollectorControllerGrpc.CollectorControllerImplBase {

    private final Map<SensorEventProto.PayloadCase, SensorEventHandler> sensorEventHandlers;
    private final Map<HubEventProto.PayloadCase, HubEventHandler> hubEventHandlers;

    public EventCollectorService(Set<SensorEventHandler> sensorEventHandlers, Set<HubEventHandler> hubEventHandlers) {
        this.sensorEventHandlers = sensorEventHandlers.stream()
                .collect(Collectors.toMap(SensorEventHandler::getMessageType, Function.identity()));
        this.hubEventHandlers = hubEventHandlers.stream()
                .collect(Collectors.toMap(HubEventHandler::getMessageType, Function.identity()));
    }

    @Override
    public void collectSensorEvent(SensorEventProto request, StreamObserver<Empty> responseObserver) {
        try {
            SensorEventProto.PayloadCase payloadCase = request.getPayloadCase();
            SensorEventHandler handler = sensorEventHandlers.get(payloadCase);

            if (handler != null) {
                log.info("Получено событие от сенсора с типом: {}", payloadCase);
                handler.handle(request);
                responseObserver.onNext(Empty.getDefaultInstance());
                responseObserver.onCompleted();
                log.info("Событие от сенсора успешно обработано: {}", payloadCase);
            } else {
                String errorMessage = "Не найден обработчик для типа данных: " + payloadCase;
                log.error(errorMessage);
                throw new IllegalArgumentException(errorMessage);
            }
        } catch (Exception e) {
            log.error("Ошибка при обработке события от сенсора: {}", e.getMessage(), e);
            responseObserver.onError(new StatusRuntimeException(Status.INTERNAL.withDescription("Внутренняя ошибка сервера: " + e.getMessage()).withCause(e)));
        }
    }

    @Override
    public void collectHubEvent(HubEventProto request, StreamObserver<Empty> responseObserver) {
        try {
            HubEventProto.PayloadCase payloadCase = request.getPayloadCase();
            HubEventHandler handler = hubEventHandlers.get(payloadCase);

            if (handler != null) {
                log.info("Получено событие от хаба с типом: {}", payloadCase);
                handler.handle(request);
                responseObserver.onNext(Empty.getDefaultInstance());
                responseObserver.onCompleted();
                log.info("Событие от хаба успешно обработано: {}", payloadCase);
            } else {
                String errorMessage = "Не найден обработчик для типа данных: " + payloadCase;
                log.error(errorMessage);
                throw new IllegalArgumentException(errorMessage);
            }
        } catch (Exception e) {
            log.error("Ошибка при обработке события от хаба: {}", e.getMessage(), e);
            responseObserver.onError(new StatusRuntimeException(Status.INTERNAL.withDescription("Внутренняя ошибка сервера: " + e.getMessage()).withCause(e)));
        }
    }
}