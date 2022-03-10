package protobuf_benchmark

import (
	"io/ioutil"
	"testing"

	"github.com/golang/protobuf/jsonpb"
	"google.golang.org/protobuf/proto"
)

func Benchmark_deserializeProtoFromJson(b *testing.B) {
	raw, err := ioutil.ReadFile("../../resources-protobuf/test_item.json")
	if err != nil {
		b.Fatal("Error reading file:", err)
	}

	// b.ResetTimer()

	for i := 0; i < b.N; i++ {
		item := Item{}
		jsonpb.UnmarshalString(string(raw), &item)
		if err != nil {
			b.Fatal("Error item unmarshal:", err)
		}
	}

	// b.StopTimer()
}

func Benchmark_deserializeProtoFromBin(b *testing.B) {
	raw, err := ioutil.ReadFile("../../resources-protobuf/test_item.pb.bin")
	if err != nil {
		b.Fatal("Error reading file:", err)
	}

	// b.ResetTimer()

	for i := 0; i < b.N; i++ {
		item := Item{}
		if err = proto.Unmarshal(raw, &item); err != nil {
			b.Fatal("Error item unmarshal:", err)
		}
	}

	// b.StopTimer()
}

func Benchmark_deserializeProtoFromBin2(b *testing.B) {
	raw, err := ioutil.ReadFile("../../resources-protobuf/test_item.pb.bin")
	if err != nil {
		b.Fatal("Error reading file:", err)
	}
	unmarshaler := proto.UnmarshalOptions{}
	b.ResetTimer()
	for i := 0; i < b.N; i++ {
		item := Item{}
		if unmarshaler.Unmarshal(raw, &item); err != nil {
			b.Fatal("Error item unmarshal:", err)
		}
	}
	 b.StopTimer()
}
