package m4gshm.benchmark.storage;

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
