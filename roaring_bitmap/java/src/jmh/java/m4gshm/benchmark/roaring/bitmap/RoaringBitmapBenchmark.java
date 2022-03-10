package m4gshm.benchmark.roaring.bitmap;


import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.roaringbitmap.RoaringBitmap;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import static java.util.Objects.requireNonNull;

@Fork(1)
@Warmup(iterations = 5, time = 1)
@Measurement(iterations = 10, time = 2)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@BenchmarkMode({Mode.AverageTime})
@State(Scope.Thread)
public class RoaringBitmapBenchmark {

    static final RoaringBitmap bitmap1 = loadFromClassPathFile("roaring-bitmap1.bin");
    static final RoaringBitmap bitmap2 = loadFromClassPathFile("roaring-bitmap2.bin");
    static final RoaringBitmap bitmap3 = loadFromClassPathFile("roaring-bitmap3.bin");
    static final RoaringBitmap bitmap4 = loadFromClassPathFile("roaring-bitmap4.bin");

    public static void main(String[] args) throws RunnerException {
        new Runner(new OptionsBuilder()
                .include(RoaringBitmapBenchmark.class.getSimpleName())
                .build()).run();
    }

    private static RoaringBitmap loadFromClassPathFile(String fileName) {
        var bitmap = new RoaringBitmap();
        try (var input = new DataInputStream(requireNonNull(RoaringBitmapBenchmark.class.getResourceAsStream("/" + fileName), "classpath:/" + fileName))) {
            bitmap.deserialize(input);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return bitmap;
    }

    @Benchmark
    public void orXorAndIntegerBitmap(Blackhole blackhole) {
        var bitmap = bitmap1.clone();
        bitmap.or(bitmap2);
        bitmap.xor(bitmap3);
        bitmap.and(bitmap4);
        blackhole.consume(bitmap);
    }

}
