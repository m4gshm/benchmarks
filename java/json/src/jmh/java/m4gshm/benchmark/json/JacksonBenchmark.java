package m4gshm.benchmark.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.util.concurrent.TimeUnit;

@Fork(value = 1)
@Warmup(iterations = 2, time = 5)
@Measurement(iterations = 2, time = 5)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@BenchmarkMode({Mode.AverageTime, Mode.SampleTime})
@State(Scope.Thread)
public class JacksonBenchmark {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    private static final MapDeserializer mapDeserializer = new MapDeserializer(objectMapper);
    private static final ItemBeanDeserializer testBeanDeserializer = new ItemBeanDeserializer(objectMapper);
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
