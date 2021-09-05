package m4gshm.benchmark.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import jdk.jfr.Label;
import jdk.jfr.Name;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import java.util.Map;

@RequiredArgsConstructor
public class MapDeserializer {
    private final ObjectMapper objectMapper;
    private final MapDeserializeEvent event = new MapDeserializeEvent();

    @SneakyThrows
    public Map deserialize(byte[] rawJson) {
        try {
            event.begin();
            return objectMapper.readValue(rawJson, Map.class);
        } finally {
            event.commit();
        }
    }

    @Name("map.deserialization")
    @Label("Map json deserialization")
    public static class MapDeserializeEvent extends jdk.jfr.Event {

    }
}
