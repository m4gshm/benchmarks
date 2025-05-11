package m4gshm.benchmark.rest.quarkus.service;

import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.Instance;
import m4gshm.benchmark.rest.java.storage.Storage;
import m4gshm.benchmark.rest.java.storage.model.Task;
import m4gshm.benchmark.rest.java.storage.model.impl.TaskImpl;
import m4gshm.benchmark.rest.java.storage.model.jpa.TaskEntity;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@Dependent
public class TaskServiceConfiguration {

    public static final String JFR_ENABLED = "jfr.enabled";

    @ConfigProperty(name = JFR_ENABLED, defaultValue = "false")
    boolean jfrEnabled;

    private <T extends Task> TaskService<T> taskService(Instance<Storage<T, String>> storage) {
        var taskService = new TaskServiceImpl<>(storage.get());
        return jfrEnabled ? new TaskServiceJFRWrapper<>(taskService) : taskService;
    }

    @Dependent
    public TaskService<TaskImpl> taskServiceTaskImpl(Instance<Storage<TaskImpl, String>> storage) {
        return taskService(storage);
    }

    @Dependent
    public TaskService<TaskEntity> taskServiceTaskEntity(Instance<Storage<TaskEntity, String>> storage) {
        return taskService(storage);
    }
}
