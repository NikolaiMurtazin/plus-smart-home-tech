package ru.yandex.practicum.kafka.config;

import lombok.RequiredArgsConstructor;
import org.apache.avro.Schema;
import org.apache.avro.io.DatumReader;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.specific.SpecificDatumReader;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Deserializer;

import java.io.ByteArrayInputStream;

@RequiredArgsConstructor
public class BaseAvroDeserializer<T extends SpecificRecordBase> implements Deserializer<T> {
    private final Schema schema;
    private final DecoderFactory decoderFactory = DecoderFactory.get();

    @Override
    public T deserialize(String topic, byte[] data) {
        if (data == null) {
            return null;
        }
        try {
            DatumReader<T> reader = new SpecificDatumReader<>(schema);
            return reader.read(null, decoderFactory.binaryDecoder(new ByteArrayInputStream(data), null));
        } catch (Exception e) {
            throw new SerializationException("Ошибка десериализации данных из топика [" + topic + "]", e);
        }
    }
}