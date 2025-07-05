package m4gshm.benchmark.rest.java.storage.model;

import java.time.LocalDateTime;
import java.util.Set;

public interface Task extends IdAware<String> {

    String getText();

    Set<String> getTags();

    LocalDateTime getDeadline();
}
