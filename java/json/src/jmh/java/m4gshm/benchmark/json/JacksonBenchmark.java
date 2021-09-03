package m4gshm.benchmark.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.util.concurrent.TimeUnit;

@Fork(value = 1)
@Warmup(iterations = 5, time = 2)
@Measurement(iterations = 10, time = 1)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@BenchmarkMode({Mode.AverageTime, Mode.SampleTime})
@State(Scope.Thread)
public class JacksonBenchmark {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    private static final MapDeserializer mapDeserializer = new MapDeserializer(objectMapper);
    private static final TestBeanDeserializer testBeanDeserializer = new TestBeanDeserializer(objectMapper);
    private static final byte[] json = JsonRepo.readTestSingleJson();

    @Benchmark
    public void deserializeJsonToMap(Blackhole blackhole) {
        blackhole.consume(mapDeserializer.deserialize(json));
    }

    @Benchmark
    public void deserializeJsonToBean(Blackhole blackhole) {
        blackhole.consume(testBeanDeserializer.deserialize(json));
    }
}
