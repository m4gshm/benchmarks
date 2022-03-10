import lombok.SneakyThrows;
import org.roaringbitmap.RoaringBitmap;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.util.Random;

public class GenerateMaps {
    public static void main(String[] args) {
        var random = new Random();
        for (int i = 1; i <= 10; i++) {
            generateToFile(random, i * 10_000_000, "resources/" + "roaring-bitmap" + i + ".bin");
        }
    }

    @SneakyThrows
    private static void generateToFile(Random random, int size, String fileName) {
        try (var out = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(fileName)))) {
            random.ints(size).collect(RoaringBitmap::new, RoaringBitmap::add, (b1, b2) -> b1.or(b2)).serialize(out);
        }
    }
}
