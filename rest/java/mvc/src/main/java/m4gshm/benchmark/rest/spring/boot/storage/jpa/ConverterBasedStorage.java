package m4gshm.benchmark.rest.spring.boot.storage.jpa;

import m4gshm.benchmark.rest.java.storage.Storage;
import m4gshm.benchmark.rest.java.storage.model.IdAware;

import java.util.List;
import java.util.function.Function;

public record ConverterBasedStorage<T extends IdAware<ID>, I extends IdAware<ID>, ID>(
        Storage<I, ID> storage, Function<T, I> in, Function<I, T> out
) implements Storage<T, ID> {

    @Override
    public T get(ID id) {
        return out.apply(storage.get(id));
    }

    @Override
    public List<T> getAll() {
        return storage.getAll().stream().map(out).toList();
    }

    @Override
    public T store(T entity) {
        return out.apply(storage.store(in.apply(entity)));
    }

    @Override
    public boolean delete(ID id) {
        return storage.delete(id);
    }
}
