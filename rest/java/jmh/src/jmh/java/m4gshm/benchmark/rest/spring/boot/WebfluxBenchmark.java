package m4gshm.benchmark.rest.spring.boot;

import m4gshm.benchmark.rest.java.model.TaskImpl;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import static java.lang.String.valueOf;

@Fork(value = 1)
@Warmup(iterations = 1, time = 5)
@Measurement(iterations = 1, time = 5)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@BenchmarkMode(Mode.AverageTime)
@State(Scope.Thread)
public class WebfluxBenchmark {
    private static final Logger log = LoggerFactory.getLogger(WebfluxBenchmark.class);
    private static final String port = "8080";
    private final AtomicLong reactiveCounter = new AtomicLong();
    private final AtomicLong counter = new AtomicLong();
    private ReactiveTaskAPI reactiveTaskAPI;
    private TaskAPI taskAPI;
    private ConfigurableApplicationContext context;

    private static void assertEquals(String expected, String actual) {
        if (!Objects.equals(expected, actual)) {
            throw new IllegalStateException("not equals expected:" + expected + ", actual:" + actual);
        }
    }

    private static void assertTrue(boolean expression) {
        if (!expression) {
            throw new IllegalStateException("expected true");
        }
    }

    @Setup
    public void start() {
        var rootUrl = "http://localhost:" + port;
        reactiveTaskAPI = TaskReactiveFeignClientFactory.newClient(rootUrl);
        taskAPI = TaskFeignClientFactory.newClient(rootUrl);
        context = SpringApplication.run(TaskWebfluxController.class);
    }

    @TearDown
    public void stop() {
        context.stop();
        context.close();
        log.info("reactive task counter " + reactiveCounter.get());
        log.info("task counter " + counter.get());
    }

    @Benchmark
    public void restCrudReactive(Blackhole blackhole) {
        var task =  TaskImpl.builder();
        var id = valueOf(reactiveCounter.incrementAndGet());
        task.id(id);
        task.text(id + "_text");


        blackhole.consume(reactiveTaskAPI.create(task.build())
                .doOnSuccess(s -> assertTrue(s.success()))
                .flatMap(created -> reactiveTaskAPI.get(id)
                        .doOnSuccess(loaded -> assertEquals(id, loaded.id()))
                        .flatMap(loaded -> reactiveTaskAPI.delete(id)
                                .doOnSuccess(s -> assertTrue(s.success()))))
                .block());
    }

    @Benchmark
    public void restCrud(Blackhole blackhole) {
        var task =  TaskImpl.builder();
        var id = valueOf(counter.incrementAndGet());
        task.id(id);
        task.text(id + "_text");

        assertTrue(taskAPI.create(task.build()).success());
        var loaded = taskAPI.get(id);
        assertEquals(id, loaded.id());
        blackhole.consume(taskAPI.delete(id));
    }
}
