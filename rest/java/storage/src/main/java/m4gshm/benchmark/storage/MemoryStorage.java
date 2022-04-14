package m4gshm.benchmark.storage;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MemoryStorage<T, ID> implements Storage<T, ID> {
    private final Map<ID, T> storage;

    public MemoryStorage(int initialCapacity, int concurrencyLevel) {
        storage = new ConcurrentHashMap<>(initialCapacity, 0.75F,
                concurrencyLevel);
    }

    @Override
    public T get(ID id) {
        return storage.get(id);
    }

    @Override
    public void store(ID id, T t) {
        storage.putIfAbsent(id, t);
    }

    @Override
    public Collection<T> getAll() {
        return storage.values();
    }

    @Override
    public void delete(ID id) {
        storage.remove(id);
    }
}
