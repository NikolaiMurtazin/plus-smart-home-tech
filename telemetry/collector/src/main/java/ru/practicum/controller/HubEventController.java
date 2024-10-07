package ru.practicum.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.config.KafkaTopics;
import ru.practicum.model.hub.HubEvent;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;

@RestController
@RequiredArgsConstructor
@RequestMapping("/events")
@Slf4j
public class HubEventController {
    private final KafkaProducer<String, HubEventAvro> hubEventProducer;

    @PostMapping("/hubs")
    public void collectHubEvent(@Valid @RequestBody HubEvent event) {
        log.info("Received hub event: {}", event);

        HubEventAvro avroEvent = convertToAvro(event);

        log.debug("Converted Avro hub event: {}", avroEvent);

        ProducerRecord<String, HubEventAvro> record =
                new ProducerRecord<>(KafkaTopics.TELEMETRY_HUBS_V1, event.getHubId(), avroEvent);
        hubEventProducer.send(record, (metadata, exception) -> {
            if (exception != null) {
                log.error("Failed to send hub event to Kafka", exception);
            } else {
                log.info("Hub event sent to Kafka, topic: {}, partition: {}, offset: {}",
                        metadata.topic(), metadata.partition(), metadata.offset());
            }
        });
    }

    private HubEventAvro convertToAvro(HubEvent event) {
        return new HubEventAvro();
    }
}
