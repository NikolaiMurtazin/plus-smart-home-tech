package ru.yandex.practicum;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class ProducerProperties {
    private Map<String, String> properties;
    private String topic;
}
