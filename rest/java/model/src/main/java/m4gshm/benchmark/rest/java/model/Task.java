package m4gshm.benchmark.rest.java.model;

import lombok.Data;

import java.time.OffsetDateTime;
import java.util.List;

@Data
public class Task {
    String id;
    String text;
    List<String> tags;
    OffsetDateTime deadline;
}
