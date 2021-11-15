package roaring_bitmap

import (
	"fmt"
	"os"
	"testing"
)
import "github.com/RoaringBitmap/roaring"

func Benchmark_OrXorAndIntegerBitmap(b *testing.B) {
	var (
		baseDir = "../../resources"
		file1   *os.File
		file2   *os.File
		file3   *os.File
		file4   *os.File
		err     error
	)

	if file1, err = os.Open(baseDir + "/roaring-bitmap1.bin"); err != nil {
		b.Fatal(err)
	}
	if file2, err = os.Open(baseDir + "/roaring-bitmap2.bin"); err != nil {
		b.Fatal(err)
	}
	if file3, err = os.Open(baseDir + "/roaring-bitmap3.bin"); err != nil {
		b.Fatal(err)
	}
	if file4, err = os.Open(baseDir + "/roaring-bitmap4.bin"); err != nil {
		b.Fatal(err)
	}

	bitmap1 := roaring.New()
	bitmap2 := roaring.New()
	bitmap3 := roaring.New()
	bitmap4 := roaring.New()
	if _, err = bitmap1.ReadFrom(file1); err != nil {
		b.Fatal(err)
	}
	if _, err = bitmap2.ReadFrom(file2); err != nil {
		b.Fatal(err)
	}
	if _, err = bitmap3.ReadFrom(file3); err != nil {
		b.Fatal(err)
	}
	if _, err = bitmap4.ReadFrom(file4); err != nil {
		b.Fatal(err)
	}

	b.ReportAllocs()
	for i := 0; i < 10; i++ {
		b.Run(fmt.Sprintf("%d", i+1), func(b *testing.B) {
			b.ResetTimer()
			for i := 0; i < b.N; i++ {
				bitmap := bitmap1.Clone()
				bitmap.Or(bitmap2)
				bitmap.Xor(bitmap3)
				bitmap.And(bitmap4)
			}
			b.StopTimer()
		})
	}

}
