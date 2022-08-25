package m4gshm.benchmark.rest.java.storage;

import io.smallrye.mutiny.Uni;
import m4gshm.benchmark.rest.java.storage.model.IdAware;

import java.util.List;

public interface MutinyStorage<T extends IdAware<ID>, ID> {

    Uni<T> get(ID id);

    Uni<T> store(T entity);

    Uni<List<T>> getAll();

    Uni<Boolean> delete(ID id);

}
