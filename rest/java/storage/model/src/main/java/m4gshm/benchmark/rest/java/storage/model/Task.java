package m4gshm.benchmark.rest.java.storage.model;

import java.util.List;

public interface Task<D> {

    String getText();

//    List<String> getTags();

    D getDeadline();
}
