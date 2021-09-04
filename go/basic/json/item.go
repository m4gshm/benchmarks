package map_benchmark

import "time"

type Item struct {
	Id      int64     `json:"id"`
	Rate    float64   `json:"rate"`
	Created time.Time `json:"created"`
	Name    string    `json:"name"`
	Type    ItemType  `json:"type"`
	Items   []Item    `json:"items"`
}
