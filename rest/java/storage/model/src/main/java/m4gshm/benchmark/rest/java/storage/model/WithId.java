package m4gshm.benchmark.rest.java.storage.model;

public interface WithId<T extends WithId<T, ID>, ID> extends IdAware<ID> {

    T withId(ID id);

}
