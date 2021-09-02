package m4gshm.benchmark.map;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Fork(value = 1)
@Warmup(iterations = 5, time = 5)
@Measurement(iterations = 3, time = 3)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@BenchmarkMode({Mode.AverageTime, Mode.SampleTime})
@State(Scope.Thread)
public class MapBenchmark {

    final Map<String, Map<String, String>> map = new HashMap<>(1);

    @Setup
    public void setup() {
    }

    @TearDown
    public void tearDown() {
        map.clear();
    }

    @Benchmark
    public void mapOfMapByComputeIfAbsent(Blackhole blackhole) {
        map.computeIfAbsent("one", k -> new HashMap<>()).put("two", "three");
        blackhole.consume(map);
    }

    @Benchmark
    public void mapOfMapBySubMap(Blackhole blackhole) {
        var key = "one";

        var subMap = map.get(key);
        if (subMap == null) {
            map.put(key, subMap = new HashMap<>());
        }
        subMap.put("two", "three");

        blackhole.consume(map);
    }

    @Benchmark
    public void mapOfMapByKey(Blackhole blackhole) {
        var key = "one";
        if (!map.containsKey(key)) {
            map.put(key, new HashMap<>());
        }
        map.get(key).put("two", "three");

        blackhole.consume(map);
    }


    @Benchmark
    public void mapOfMapByKeyUpdate(Blackhole blackhole) {
        var key = "one";
        if (!map.containsKey(key)) {
            map.put(key, new HashMap<>());
        }
        var subMap = map.get(key);
        subMap.put("two", "three");
        map.put(key, subMap);

        blackhole.consume(map);
    }


}
