package m4gshm.benchmark.storage;

import java.util.Collection;

public interface Storage<T, ID> {
    T get(ID id);

    void store(ID id, T t);

    Collection<T> getAll();

    void delete(ID id);
}
