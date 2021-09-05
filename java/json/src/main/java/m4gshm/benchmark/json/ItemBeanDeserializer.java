package m4gshm.benchmark.json;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jdk.jfr.Label;
import jdk.jfr.Name;
import lombok.SneakyThrows;

public class ItemBeanDeserializer {
    private final ObjectMapper objectMapper;
    private final ItemDeserializeEvent event = new ItemDeserializeEvent();

    public ItemBeanDeserializer(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper.registerModule(new JavaTimeModule());
    }

    @SneakyThrows
    public Item deserialize(byte[] rawJson) {
        try {
            event.begin();
            return objectMapper.readValue(rawJson, Item.class);
        } finally {
            event.commit();
        }
    }

    @Name("item.deserialization")
    @Label("Item json deserialization")
    public static class ItemDeserializeEvent extends jdk.jfr.Event {

    }
}
