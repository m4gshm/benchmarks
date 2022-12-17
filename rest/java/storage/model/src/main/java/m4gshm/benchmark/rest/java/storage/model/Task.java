package m4gshm.benchmark.rest.java.storage.model;

import java.util.Set;

public interface Task<D> {

    String getText();

    Set<String> getTags();

    D getDeadline();
}
