package m4gshm.benchmark.rest.ktor.graalvm;

import java.util.ArrayList;

public interface Storage<T, ID> {
    T get(ID id);
    void store(ID id, T t);
    ArrayList<T> getAll();
    boolean delete(ID id);
}