package m4gshm.benchmark.protobuf;

import lombok.SneakyThrows;

public class ProtobufRepo {

    public static final byte[] rawTestSingleJson = readTestSingleJson();
    public static final byte[] rawTestSingleBin = readTestSingleBin();

    @SneakyThrows
    public static byte[] readTestSingleJson() {
        return ProtobufRepo.class.getResourceAsStream("/test_item.json").readAllBytes();
    }

    @SneakyThrows
    public static byte[] readTestSingleBin() {
        return ProtobufRepo.class.getResourceAsStream("/test_item.pb.bin").readAllBytes();
    }
}
