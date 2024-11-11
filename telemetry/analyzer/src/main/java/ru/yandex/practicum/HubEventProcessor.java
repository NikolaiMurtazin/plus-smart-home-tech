package ru.yandex.practicum;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.errors.WakeupException;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.consumer.KafkaProperties;
import ru.yandex.practicum.kafka.telemetry.event.*;
import ru.yandex.practicum.service.ScenarioService;
import ru.yandex.practicum.service.SensorService;

import java.time.Duration;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class HubEventProcessor implements Runnable {

    private final Consumer<String, HubEventAvro> consumer;
    private final KafkaProperties kafkaProperties;
    private final SensorService sensorService;
    private final ScenarioService scenarioService;
    private static long POLL_DURATION;

    @PostConstruct
    private void init() {
        POLL_DURATION = kafkaProperties.getPollDuration();
    }

    @Override
    public void run() {
        log.info("HubEventProcessor processor started");
        Runtime.getRuntime().addShutdownHook(new Thread(consumer::wakeup));
        try {
            consumer.subscribe(List.of(kafkaProperties.getHubs().getTopic()));
            while (true) {
                ConsumerRecords<String, HubEventAvro> records = consumer.poll(Duration.ofMillis(POLL_DURATION));
                for (ConsumerRecord<String, HubEventAvro> record : records) {
                    sendToService(record);
                }
                consumer.commitSync();
            }
        } catch (WakeupException e) {
        } catch (Exception e) {
            log.error("Ошибка во время обработки событий от хаба", e);
        } finally {
            log.info("Закрываем консьюмер");
            consumer.close();
        }
    }


    private void sendToService(ConsumerRecord<String, HubEventAvro> record) {
        HubEventAvro event = record.value();
        switch (event.getPayload()) {
            case DeviceAddedEventAvro deviceAddedEvent ->
                    sensorService.addSensor(deviceAddedEvent.getId(), event.getHubId());
            case DeviceRemovedEventAvro deviceRemovedEvent ->
                    sensorService.removeSensor(deviceRemovedEvent.getId(), event.getHubId());
            case ScenarioAddedEventAvro scenarioAddedEvent ->
                    scenarioService.addScenario(scenarioAddedEvent, event.getHubId());
            case ScenarioRemovedEventAvro scenarioRemovedEvent ->
                    scenarioService.deleteScenario(scenarioRemovedEvent.getName(), event.getHubId());
            default -> log.warn("Unknown event type: {}", event.getPayload().getClass().getName());
        }
    }

}