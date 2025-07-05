package m4gshm.benchmark.rest.quarkus.service;

import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.Instance;
import m4gshm.benchmark.rest.java.storage.MutinyStorage;
import m4gshm.benchmark.rest.java.storage.model.Task;
import m4gshm.benchmark.rest.java.storage.model.impl.TaskImpl;
import m4gshm.benchmark.rest.java.storage.model.jpa.TaskEntity;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@Dependent
public class ReactiveTaskServiceConfiguration {

    public static final String JFR_ENABLED = "jfr.enabled";

    @ConfigProperty(name = JFR_ENABLED, defaultValue = "false")
    boolean jfrEnabled;

    private <T extends Task> ReactiveTaskService<T> taskService(Instance<MutinyStorage<T, String>> storage) {
        var taskService = new ReactiveTaskServiceImpl<>(storage.get());
        return jfrEnabled ? new ReactiveTaskServiceJFRWrapper<>(taskService) : taskService;
    }

    @Dependent
    public ReactiveTaskService<TaskImpl> taskServiceTaskImpl(Instance<MutinyStorage<TaskImpl, String>> storage) {
        return taskService(storage);
    }

    @Dependent
    public ReactiveTaskService<TaskEntity> taskServiceTaskEntity(Instance<MutinyStorage<TaskEntity, String>> storage) {
        return taskService(storage);
    }
}
