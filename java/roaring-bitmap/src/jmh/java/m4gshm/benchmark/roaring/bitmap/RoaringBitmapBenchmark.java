package m4gshm.benchmark.json;


import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.roaringbitmap.RoaringBitmap;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Fork(value = 1)
@Warmup(iterations = 2, time = 5)
@Measurement(iterations = 2, time = 5)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@BenchmarkMode({Mode.AverageTime})
@State(Scope.Thread)
public class RoaringBitmapBenchmark {

    RoaringBitmap bitmap1;
    RoaringBitmap bitmap2;
    RoaringBitmap bitmap3;

    private static RoaringBitmap loadFromClassPathFiel(String fileName) {
        var bitmap = new RoaringBitmap();
        try (var input = new DataInputStream(new BufferedInputStream(RoaringBitmapBenchmark.class.getResourceAsStream("/" + fileName)))) {
            bitmap.deserialize(input);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return bitmap;
    }

    @Setup
    public void setup() {
        bitmap1 = loadFromClassPathFiel("roaring-bitmap1.bin");
        bitmap2 = loadFromClassPathFiel("roaring-bitmap2.bin");
        bitmap3 = loadFromClassPathFiel("roaring-bitmap3.bin");
    }

    @Benchmark
    public void orXorAndIntegerBitmap(Blackhole blackhole) {
        bitmap1.xor(bitmap2);
        bitmap1.and(bitmap3);
    }

}
