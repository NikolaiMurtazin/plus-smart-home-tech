package ru.yandex.practicum.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.exception.AlreadyExistException;
import ru.yandex.practicum.exception.NotFoundException;
import ru.yandex.practicum.model.Sensor;
import ru.yandex.practicum.repository.SensorRepository;

@Service
@RequiredArgsConstructor
public class SensorService {
    private final SensorRepository sensorRepository;

    @Transactional
    public Sensor addSensor(String deviceId, String hubId) {
        if (isSensorExist(deviceId)) {
            throw new AlreadyExistException("Sensor with id " + deviceId + " already exists");
        }
        return sensorRepository.save(new Sensor(deviceId, hubId));
    }

    @Transactional
    public void removeSensor(String deviceId, String hubId) {
        Sensor savedSensor = findSensorById(deviceId);
        sensorRepository.delete(savedSensor);
    }


    private Sensor findSensorById(String id) {
        return sensorRepository.findById(id).orElseThrow(() -> new NotFoundException("Not found sensor with id " + id));
    }

    private boolean isSensorExist(String deviceId) {
        return sensorRepository.existsById(deviceId);
    }

}
