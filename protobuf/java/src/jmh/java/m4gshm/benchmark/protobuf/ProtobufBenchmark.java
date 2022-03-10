package m4gshm.benchmark.protobuf;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.util.JsonFormat.Parser;
import m4gshm.benchmark.protobuf.ItemOuterClass.Item;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.util.concurrent.TimeUnit;

import static com.google.protobuf.util.JsonFormat.parser;
import static java.nio.charset.StandardCharsets.UTF_8;
import static m4gshm.benchmark.protobuf.ProtobufRepo.rawTestSingleBin;
import static m4gshm.benchmark.protobuf.ProtobufRepo.rawTestSingleJson;

@Fork(value = 1)
@Warmup(iterations = 2, time = 5)
@Measurement(iterations = 2, time = 5)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@BenchmarkMode(Mode.AverageTime)
@State(Scope.Thread)
public class ProtobufBenchmark {

    private static final String json = new String(rawTestSingleJson, UTF_8);
    private static final Parser parser = parser();

    @Benchmark
    public void deserializeProtoFromJson(Blackhole blackhole) throws InvalidProtocolBufferException {
        var builder = Item.newBuilder();
        parser.merge(json, builder);
        var item = builder.build();
        blackhole.consume(item);
    }

    @Benchmark
    public void deserializeProtoFromBin(Blackhole blackhole) throws InvalidProtocolBufferException {
        var item = Item.parseFrom(rawTestSingleBin);
        blackhole.consume(item);
    }
}
