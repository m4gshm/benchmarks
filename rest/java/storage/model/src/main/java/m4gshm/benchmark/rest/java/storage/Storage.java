package m4gshm.benchmark.rest.java.storage;

import m4gshm.benchmark.rest.java.storage.model.IdAware;
import m4gshm.benchmark.rest.java.storage.model.impl.TaskImpl;

import java.util.List;

public interface Storage<T extends IdAware<ID>, ID> {
    T get(ID id);

    List<T> getAll();

    default T store(T entity) {
        return store(entity, entity.getId());
    }

    T store(T task, ID id);

    boolean delete(ID id);

}
