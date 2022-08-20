package m4gshm.benchmark.rest.java.model;

import java.util.List;

public interface Task<T extends Task<T, D>, D> {
    String getId();

    String getText();

    List<String> getTags();

    D getDeadline();

    T withId(String id);
}
