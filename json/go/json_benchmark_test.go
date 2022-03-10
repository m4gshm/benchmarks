package map_benchmark

import (
	"encoding/json"
	"io/ioutil"
	"os"
	"testing"
)

func Benchmark_UnmarshalToMap(b *testing.B) {

	jsonFile, err := openJsonFile()
	if err != nil {
		b.Fatal(err)
	}

	defer jsonFile.Close()

	rawJson, err := ioutil.ReadAll(jsonFile)
	if err != nil {
		b.Fatal(err)
	}

	b.ResetTimer()

	for i := 0; i < b.N; i++ {
		dest := make(map[string]interface{})
		err = json.Unmarshal(rawJson, &dest)
		if err != nil {
			b.Fatal(err)
		}
	}

	b.StopTimer()
}

func Benchmark_UnmarshalToStruct(b *testing.B) {

	jsonFile, err := openJsonFile()
	if err != nil {
		b.Fatal(err)
	}

	defer jsonFile.Close()

	rawJson, err := ioutil.ReadAll(jsonFile)
	if err != nil {
		b.Fatal(err)
	}

	b.ResetTimer()

	for i := 0; i < b.N; i++ {
		var dest Item
		err = json.Unmarshal(rawJson, &dest)
		if err != nil {
			b.Fatal(err)
		}
	}

	b.StopTimer()
}

func openJsonFile() (*os.File, error) {
	return os.Open("../../resources-json/test_item.json")
}
