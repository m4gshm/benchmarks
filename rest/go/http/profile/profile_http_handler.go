package profile

import (
	"fmt"
	"io"
	"net/http"
	"os"
	"runtime/pprof"
	"sync/atomic"

	"github.com/rs/zerolog/log"
)

var profFile = atomic.Pointer[os.File]{}

func Start(w http.ResponseWriter, r *http.Request) {
	tempDir := os.TempDir()
	if _, err := os.Stat(tempDir); os.IsNotExist(err) {
		if err := os.Mkdir(tempDir, os.ModeDir); err != nil {
			log.Err(err).Msg("temp dir creation error")
		}
	}
	if file, err := os.CreateTemp(tempDir, "*-profile.pprof"); err != nil {
		serveError(w, http.StatusInternalServerError, fmt.Errorf("could not create temporary file: %w", err))
	} else if profFile.CompareAndSwap(nil, file) {
		if err := pprof.StartCPUProfile(file); err != nil {
			serveError(w, http.StatusInternalServerError, fmt.Errorf("could not enable CPU profiling: %w", err))
		} else {
			w.WriteHeader(http.StatusCreated)
		}
	} else {
		w.WriteHeader(http.StatusNoContent)
	}
}

func Stop(w http.ResponseWriter, r *http.Request) {
	file := profFile.Load()
	if file != nil && profFile.CompareAndSwap(file, nil) {
		pprof.StopCPUProfile()
		_, _ = file.Seek(0, io.SeekStart)
		bytes, err := io.ReadAll(file)
		_ = file.Close()
		if err != nil {
			serveError(w, http.StatusInternalServerError, fmt.Errorf("could not read temporary file: %w", err))
		} else {
			w.Header().Set("X-Content-Type-Options", "nosniff")
			w.Header().Set("Content-Type", "application/octet-stream")
			w.Header().Set("Content-Disposition", `attachment; filename="profile"`)
			if _, err = w.Write(bytes); err != nil {
				serveError(w, http.StatusInternalServerError, fmt.Errorf("could not write response: %w", err))
			} else {
				w.WriteHeader(http.StatusOK)
			}
		}
	} else {
		w.WriteHeader(http.StatusNoContent)
	}
}

func serveError(w http.ResponseWriter, status int, err error) {
	log.Err(err).Msg("http server error")
	w.Header().Set("Content-Type", "text/plain; charset=utf-8")
	w.Header().Set("X-Go-Pprof", "1")
	w.Header().Del("Content-Disposition")
	w.WriteHeader(status)
	_, _ = fmt.Fprintln(w, err.Error())
}