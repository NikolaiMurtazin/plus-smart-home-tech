package ru.yandex.practicum.serializer;

import org.apache.avro.io.BinaryEncoder;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.specific.SpecificDatumWriter;
import org.apache.kafka.common.serialization.Serializer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.commons.lang3.SerializationException;

@Slf4j
public class AvroSerializer<T extends SpecificRecordBase> implements Serializer<T> {
    @Override
    public byte[] serialize(String topic, T data) {
        if (data == null) {
            return null;
        }

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            BinaryEncoder encoder = EncoderFactory.get().binaryEncoder(out, null);
            SpecificDatumWriter<T> writer = new SpecificDatumWriter<>(data.getSchema());
            writer.write(data, encoder);
            encoder.flush();
            return out.toByteArray();
        } catch (IOException e) {
            log.error("Ошибка сериализации Avro-сообщения для топика [{}]", topic, e);
            throw new SerializationException("Ошибка сериализации Avro-сообщения для топика [" + topic + "]", e);
        }
    }
}
