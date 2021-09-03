package m4gshm.benchmark.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import lombok.SneakyThrows;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Fork(value = 1)
@Warmup(iterations = 1, time = 10)
@Measurement(iterations = 10, time = 1)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@BenchmarkMode({Mode.AverageTime, Mode.SampleTime})
@State(Scope.Thread)
public class JacksonBenchmark {

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final ObjectReader reader = objectMapper.readerFor(Map.class);
    private byte[] rawSrc;

    @Setup(value = Level.Iteration)
    public void setup() {
        var stream = getClass().getResourceAsStream("/test_single.json");
        try {
            rawSrc = stream.readAllBytes();
            reader.readValue(rawSrc);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Benchmark
    public void deserializeJsonToMap(Blackhole blackhole) {
        Map map;
        try {
            map = reader.readValue(rawSrc);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        blackhole.consume(map);
    }
}
