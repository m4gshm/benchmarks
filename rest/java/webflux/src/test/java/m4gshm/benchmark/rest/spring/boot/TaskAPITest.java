package m4gshm.benchmark.rest.spring.boot;

import m4gshm.benchmark.rest.java.model.TaskImpl;
import org.junit.Ignore;
import org.junit.Test;

public class TaskAPITest {
    @Test
    @Ignore
    public void reactiveCreateTest() {
        var taskAPI = TaskReactiveFeignClientFactory.newClient("http://localhost:8080");
        for (var i = 0; i < 2_000; i++) {
            taskAPI.list().subscribe();
        }
    }

    @Test
    @Ignore
    public void createTest() {
        var taskAPI = TaskFeignClientFactory.newClient("http://localhost:8080");
        for (var i = 0; i < 2_000; i++) {
            taskAPI.create(TaskImpl.builder().build());
        }
    }
}
