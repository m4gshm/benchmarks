package m4gshm.benchmark.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import java.util.Map;

@RequiredArgsConstructor
public class MapDeserializer {
    private final ObjectMapper objectMapper;

    @SneakyThrows
    public Map deserialize(byte[] rawJson) {
        return objectMapper.readValue(rawJson, Map.class);
    }

}
