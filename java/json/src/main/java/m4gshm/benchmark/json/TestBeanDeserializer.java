package m4gshm.benchmark.json;


import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

@RequiredArgsConstructor
public class TestBeanDeserializer {
    private final ObjectMapper objectMapper;

    @SneakyThrows
    public TestBean deserialize(byte[] rawJson) {
        return objectMapper.readValue(rawJson, TestBean.class);
    }

}
