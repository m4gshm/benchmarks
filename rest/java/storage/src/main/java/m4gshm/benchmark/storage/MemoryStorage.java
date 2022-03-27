package m4gshm.benchmark.storage;

import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class MemoryStorage<T, ID> implements Storage<T, ID> {
    private final Map<ID, T> storage = new ConcurrentHashMap<>();

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
