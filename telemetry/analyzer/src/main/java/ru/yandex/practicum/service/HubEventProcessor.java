package ru.yandex.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import ru.yandex.practicum.config.KafkaConsumerConfig;
import ru.yandex.practicum.kafka.telemetry.event.*;

import java.time.Duration;
import java.util.Collections;

@Slf4j
@Component
@RequiredArgsConstructor
public class HubEventProcessor implements Runnable {

    private final ScenarioService scenarioService;
    private final SensorService sensorService;

    @Override
    public void run() {
        try (KafkaConsumer<String, HubEventAvro> consumer = KafkaConsumerConfig.kafkaConsumerHubEvent()) {
            consumer.subscribe(Collections.singletonList("telemetry.hubs.v1"));

            while (!Thread.currentThread().isInterrupted()) {
                ConsumerRecords<String, HubEventAvro> records = consumer.poll(Duration.ofMillis(100));

                for (ConsumerRecord<String, HubEventAvro> record : records) {
                    try {
                        onMessage(record);
                    } catch (Exception e) {
                        log.error("Error processing event: {}", record, e);
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error in Kafka consumer", e);
        }
    }


    @KafkaListener(topics = "${kafka.topics.telemetry-hubs}", groupId = "${spring.kafka.consumer.group-id.telemetry-hubs}")
    public void onMessage(ConsumerRecord<String, HubEventAvro> record) {
        HubEventAvro event = record.value();

        switch (event.getPayload()) {
            case DeviceAddedEventAvro deviceAddedEvent ->
                    sensorService.addSensor(deviceAddedEvent.getId(), event.getHubId());
            case DeviceRemovedEventAvro deviceRemovedEvent ->
                    sensorService.removeSensor(deviceRemovedEvent.getId(), event.getHubId());
            case ScenarioAddedEventAvro scenarioAddedEvent ->
                    scenarioService.addScenario(scenarioAddedEvent, event.getHubId());
            case ScenarioRemovedEventAvro scenarioRemovedEvent ->
                    scenarioService.deleteScenario(scenarioRemovedEvent.getName());
            case null, default -> log.warn("Unknown event type: {}", event.getPayload().getClass().getName());
        }
    }
}
