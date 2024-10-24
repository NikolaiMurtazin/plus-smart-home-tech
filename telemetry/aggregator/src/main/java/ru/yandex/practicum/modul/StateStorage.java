package ru.yandex.practicum.modul;

import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class StateStorage {

    private final Map<String, SensorsSnapshotAvro> snapshots = new ConcurrentHashMap<>();

    public SensorsSnapshotAvro getSnapshot(String hubId) {
        return snapshots.get(hubId);
    }

    public void updateSnapshot(String hubId, SensorsSnapshotAvro snapshot) {
        snapshots.put(hubId, snapshot);
    }
}
