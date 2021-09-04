package m4gshm.benchmark.json;

import lombok.SneakyThrows;

public class JsonRepo {

    public static final byte[] rawTestSingleJson = readTestSingleJson();

    @SneakyThrows
    public static byte[] readTestSingleJson() {
        return JsonRepo.class.getResourceAsStream("/test_item.json").readAllBytes();
    }
}
