package m4gshm.benchmark.protobuf;

import com.google.protobuf.util.JsonFormat;
import lombok.SneakyThrows;
import org.junit.Test;

import static java.nio.charset.StandardCharsets.UTF_8;
import static m4gshm.benchmark.protobuf.ProtobufRepo.rawTestSingleBin;
import static m4gshm.benchmark.protobuf.ProtobufRepo.rawTestSingleJson;
import static org.junit.Assert.assertNotNull;

public class ProtobufTest {

    @Test
    @SneakyThrows
    public void deserializeJsonTest() {

        var json = new String(rawTestSingleJson, UTF_8);
        var builder = ItemOuterClass.Item.newBuilder();
        JsonFormat.parser().merge(json, builder);
        var item = builder.build();

        assertNotNull(item);

//        item.writeTo(new FileOutputStream("test_item.pb.bin"));
    }

    @Test
    @SneakyThrows
    public void deserializeBinTest() {
        var item = ItemOuterClass.Item.parseFrom(rawTestSingleBin);
        assertNotNull(item);
    }
}
