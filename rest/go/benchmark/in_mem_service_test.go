package benchmark

import (
	"bytes"
	"context"
	"encoding/json"
	"fmt"
	"io/ioutil"
	"log"
	"net/http"
	"strconv"
	"sync"
	"testing"
	"time"

	"github.com/ztrue/shutdown"

	"benchmark/rest/storage/memory"
	"benchmark/rest/task"
)

var (
	inProcAddr = "localhost:6783"
)

func init() {
	var inProcServer = startServer(inProcAddr)
	shutdown.Add(func() {
		if ierr := inProcServer.Shutdown(context.Background()); ierr != nil {
			fmt.Println(ierr.Error())
		}
	})
}

func Benchmark_golang_inproc_rest_inmem_create(b *testing.B) {
	b.ResetTimer()
	for i := 0; i < b.N; i++ {
		id := strconv.Itoa(i)
		resp, err := createTaskCall(inProcAddr, newTask(id))
		assertResponseStatus(b, err, resp)

		resp, err = getTaskCall(inProcAddr, id)
		assertResponseStatus(b, err, resp)

		resp, err = deleteTaskCall(inProcAddr, id)
		assertResponseStatus(b, err, resp)
	}
	b.StopTimer()

	fmt.Println("task counter", b.N)
}

func assertResponseStatus(b *testing.B, err error, resp *http.Response) {
	if err != nil {
		b.Fatal(err)
	} else if resp.StatusCode != 200 {
		if rawResponsePayload, err := ioutil.ReadAll(resp.Body); err != nil {
			b.Fatal(err)
		} else {
			b.Fatal(resp.Status + ", " + string(rawResponsePayload))
		}
	}
}

var httpClient = &http.Client{Transport: &http.Transport{
	MaxIdleConns:        200,
	MaxIdleConnsPerHost: 200,
}}

func createTaskCall(addr string, t task.Task) (*http.Response, error) {
	rawPayload, err := json.Marshal(t)
	if err != nil {
		return nil, err
	}
	return httpClient.Post("http://"+addr+"/task", "application/json", bytes.NewReader(rawPayload))
}

func getTaskCall(addr string, id string) (*http.Response, error) {
	return httpClient.Get("http://" + addr + "/task/" + id)
}

func deleteTaskCall(addr string, id string) (*http.Response, error) {
	req, err := http.NewRequest("DELETE", "http://"+addr+"/task/"+id, http.NoBody)
	if err != nil {
		return nil, err
	}
	return httpClient.Do(req)
}

func newTask(id string) task.Task {
	d := time.Now()
	return task.Task{Id: id, Text: "text_" + id, Deadline: &d}
}

func startServer(addr string) *http.Server {
	server := task.NewTaskServer[*task.Task, string](addr, memory.NewMemoryStorage[*task.Task, string](), task.StringID)
	var waiter sync.WaitGroup
	waiter.Add(1)
	go func() {
		waiter.Done()
		err := server.ListenAndServe()
		if err != nil {
			log.Println(err)
		}
	}()
	waiter.Wait()
	return server
}
