package m4gshm.benchmark.rest.java.storage;

import m4gshm.benchmark.rest.java.storage.model.IdAware;

import java.util.List;

public interface Storage<T extends IdAware<ID>, ID> {
    T get(ID id);

    List<T> getAll();

    T store(T entity);

    boolean delete(ID id);
}
