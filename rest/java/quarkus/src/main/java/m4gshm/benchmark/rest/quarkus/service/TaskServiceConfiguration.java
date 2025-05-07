package m4gshm.benchmark.rest.quarkus.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.Instance;
import m4gshm.benchmark.rest.java.storage.Storage;
import m4gshm.benchmark.rest.java.storage.model.Task;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@Dependent
public class TaskServiceConfiguration {

    public static final String JFR_ENABLED = "jfr.enabled";

    @ConfigProperty(name = JFR_ENABLED, defaultValue = "false")
    boolean jfrEnabled;

    @ApplicationScoped
    public TaskService<Task> taskService(Instance<Storage<Task, String>> storage) {
        var taskService = new TaskServiceImpl(storage.get());
        return jfrEnabled ? new TaskServiceJFRWrapper<>(taskService) : taskService;
    }
}
