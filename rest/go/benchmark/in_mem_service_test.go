package benchmark

import (
	"bytes"
	"context"
	"encoding/json"
	"errors"
	"fmt"
	"io/ioutil"
	"log"
	"net/http"
	"os"
	"os/exec"
	"strconv"
	"sync"
	"testing"
	"time"

	"github.com/ztrue/shutdown"

	"benchmark/rest/task"
)

var (
	springMvcPort = "6782"
	springMvcAddr = "localhost:" + springMvcPort
	inProcAddr    = "localhost:6783"
	golangAddr    = "localhost:6784"
)

func init() {
	var inProcServer = startServer(inProcAddr)

	//golangCmd := exec.Command("../bin/server", golangAddr)
	//if err := golangCmd.Start(); err != nil {
	//	log.Fatalln(err)
	//}
	//if err := checkCalls(golangAddr); err != nil {
	//	log.Fatalln(err)
	//}
	//springMvcCmd, err := runJavaServer(springMvcPort)
	//if err != nil {
	//	log.Fatalln(err)
	//}
	//if err = checkCalls(springMvcAddr); err != nil {
	//	log.Fatalln(err)
	//}
	//
	//warmCalls(springMvcAddr)
	shutdown.Add(func() {
		if ierr := inProcServer.Shutdown(context.Background()); ierr != nil {
			fmt.Println(ierr.Error())
		}
		//if ierr := interrupt(springMvcCmd); ierr != nil {
		//	fmt.Println(ierr.Error())
		//}
		//if ierr := interrupt(golangCmd); ierr != nil {
		//	fmt.Println(ierr.Error())
		//}
	})
}

func Benchmark_golang_inproc_rest_inmem_create(b *testing.B) {
	b.ResetTimer()
	for i := 0; i < b.N; i++ {
		resp, err := createTaskCall(inProcAddr, newTask(strconv.Itoa(i)))
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
	b.StopTimer()
}

func Benchmark_golang_server_rest_inmem_create(b *testing.B) {
	b.SkipNow()
	b.ResetTimer()
	for i := 0; i < b.N; i++ {
		resp, err := createTaskCall(golangAddr, newTask(strconv.Itoa(i)))
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
	b.StopTimer()
}

func Benchmark_java_springmvc_rest_inmem_create(b *testing.B) {
	b.SkipNow()
	b.ResetTimer()
	for i := 0; i < b.N; i++ {
		resp, err := createTaskCall(springMvcAddr, newTask(strconv.Itoa(i)))
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
	b.StopTimer()
}

func runJavaServer(port string) (*exec.Cmd, error) {
	jarFilePath := "../../java/build/libs/java.jar"
	cmd := exec.Command("java", "-Dserver.port="+port, "-jar", jarFilePath)
	errFile, err := os.Create("err.txt")
	if err != nil {
		log.Fatalln(err)
	}
	cmd.Stderr = errFile

	//outFile, err := os.Create("out.txt")
	//if err != nil {
	//	log.Fatalln(err)
	//}
	//cmd.Stdout = outFile

	if err := cmd.Start(); err != nil {
		return nil, err
	}
	if errRaw, err := ioutil.ReadAll(errFile); err != nil {
		_ = interrupt(cmd)
		return nil, err
	} else if len(errRaw) > 0 {
		_ = interrupt(cmd)
		return nil, errors.New(string(errRaw))
	}

	return cmd, err
}

func interrupt(cmd *exec.Cmd) error {
	return cmd.Process.Signal(os.Interrupt)
}

func checkCalls(addr string) error {
	var err error
	for i := 0; i < 100; i++ {
		var resp *http.Response
		if resp, err = createTaskCall(addr, newTask("-1")); err != nil {
			time.Sleep(100 * time.Millisecond)
		} else if resp.StatusCode == 200 {
			deleteTaskCall(addr, "-1")
			break
		}
	}
	if err != nil {
		return err
	}
	return nil
}

func warmCalls(addr string) {
	start := time.Now()
	log.Println("start warm", start)
	wg := sync.WaitGroup{}
	for j := 0; j < 10; j++ {
		wg.Add(1)
		go func() {
			for i := 0; i < 100; i++ {
				id := strconv.Itoa(i)
				if resp, err := createTaskCall(addr, newTask(id)); err == nil && resp.StatusCode == 200 {
					if dr, derr := deleteTaskCall(addr, id); derr != nil {
						log.Println("error:", derr.Error())
					} else if dr.StatusCode != 200 {
						log.Println("response status error:", dr.Status)
					}
				}
			}
			wg.Done()
		}()
	}
	wg.Wait()
	finish := time.Now()
	log.Println("finish warm", finish)
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

func deleteTaskCall(addr string, id string) (*http.Response, error) {
	req, err := http.NewRequest("DELETE", "http://"+addr+"/task/"+id, http.NoBody)
	if err != nil {
		return nil, err
	}
	return httpClient.Do(req)
}

func newTask(id string) task.Task {
	return task.Task{Id: id, Text: "text_" + id, Deadline: time.Now()}
}

func startServer(addr string) *http.Server {
	server := task.NewTaskServer(addr, task.NewMemoryStorage())
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
