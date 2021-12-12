package m4gshm.benchmark.json;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static java.util.List.copyOf;


public class Item {

    private final Integer id;
    private final Double rate;
    private final LocalDateTime created;
    private final String name;
    private final Type type;
    private final List<Item> items;

    @JsonCreator
    public Item(
            @JsonProperty("id") Integer id,
            @JsonProperty("rate") Double rate,
            @JsonProperty("created") LocalDateTime created,
            @JsonProperty("name") String name,
            @JsonProperty("type") Type type,
            @JsonProperty("items") List<Item> items
    ) {
        this.id = id;
        this.rate = rate;
        this.created = created;
        this.name = name;
        this.type = type;
        this.items = items != null ? copyOf(items) : null;
    }

    public enum Type {
        basic, extended;
    }
}
