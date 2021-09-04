package map_benchmark

import (
	"encoding/json"
	"fmt"
	"io/ioutil"
	"os"
	"testing"
)

func Benchmark_UnmarshalToMap(b *testing.B) {

	jsonFile, err := os.Open("../../../resources/test_item.json")
	if err != nil {
		b.Fatal(err)
	}

	defer jsonFile.Close()

	rawJson, err := ioutil.ReadAll(jsonFile)
	if err != nil {
		b.Fatal(err)
	}

	dest := make(map[string]interface{})
	
	b.ResetTimer()
	for i := 0; i < b.N; i++ {
		err = json.Unmarshal(rawJson, &dest)
		if err != nil {
			b.Fatal(err)
		}
	}

	b.StopTimer()

	fmt.Printf("%v\n", b.N)

}


func Benchmark_UnmarshalToMapRefreshable(b *testing.B) {

	jsonFile, err := os.Open("../../../resources/test_item.json")
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

	fmt.Printf("%v\n", b.N)

}
