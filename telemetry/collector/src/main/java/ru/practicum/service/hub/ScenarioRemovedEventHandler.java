package ru.practicum.service.hub;

import org.springframework.stereotype.Service;
import ru.practicum.config.KafkaEventProducer;
import ru.practicum.config.KafkaTopics;
import ru.practicum.controller.BaseHubEventHandler;
import ru.practicum.model.hub.HubEvent;
import ru.practicum.model.hub.HubEventType;
import ru.practicum.model.hub.ScenarioRemovedEvent;
import ru.yandex.practicum.kafka.telemetry.event.ScenarioRemovedEventAvro;

@Service
public class ScenarioRemovedEventHandler extends BaseHubEventHandler<ScenarioRemovedEventAvro> {

    public ScenarioRemovedEventHandler(KafkaEventProducer producer, KafkaTopics kafkaTopics) {
        super(producer, kafkaTopics);
    }

    @Override
    public HubEventType getMessageType() {
        return HubEventType.SCENARIO_REMOVED;
    }

    @Override
    protected ScenarioRemovedEventAvro mapToAvro(HubEvent event) {
        ScenarioRemovedEvent scenarioRemovedEvent = (ScenarioRemovedEvent) event;

        return ScenarioRemovedEventAvro.newBuilder()
                .setName(scenarioRemovedEvent.getName())
                .build();
    }
}
