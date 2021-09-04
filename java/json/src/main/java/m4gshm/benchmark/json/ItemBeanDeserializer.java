package m4gshm.benchmark.json;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.SneakyThrows;

public class ItemBeanDeserializer {
    private final ObjectMapper objectMapper;

    public ItemBeanDeserializer(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper.registerModule(new JavaTimeModule());
    }

    @SneakyThrows
    public Item deserialize(byte[] rawJson) {
        return objectMapper.readValue(rawJson, Item.class);
    }

}
