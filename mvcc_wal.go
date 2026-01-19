package main

import (
	"fmt"
	"os"
)

// MVCC
type Version struct {
	value   int
	beginTx int
	endTx   int // 0 = infinity
}

// WAL
type WAL struct {
	file *os.File
}

func NewWAL(path string) *WAL {
	f, err := os.OpenFile(path, os.O_CREATE|os.O_APPEND|os.O_WRONLY, 0644)
	if err != nil {
		panic(err)
	}
	return &WAL{file: f}
}

func (w *WAL) Log(record string) {
	if _, err := w.file.WriteString(record + "\n"); err != nil {
		panic(err)
	}
}

func (w *WAL) Sync() {
	if err := w.file.Sync(); err != nil {
		panic(err)
	}
}

// store
type Store struct {
	versions []*Version
	nextTx   int
	wal      *WAL
}

func NewStore() *Store {
	return &Store{
		versions: make([]*Version, 0, 1024),
		nextTx:   1,
		wal:      NewWAL("wal_go.log"),
	}
}

func (s *Store) Begin() int {
	tx := s.nextTx
	s.nextTx++

	// WAL: BEGIN
	s.wal.Log(fmt.Sprintf("BEGIN %d", tx))
	s.wal.Sync()

	return tx
}

func (s *Store) Read(tx int) int {
	for i := len(s.versions) - 1; i >= 0; i-- {
		v := s.versions[i]
		if v.beginTx <= tx && (v.endTx == 0 || v.endTx > tx) {
			return v.value
		}
	}
	return 0
}

func (s *Store) Write(tx int, value int) {
	// WAL: WRITE
	s.wal.Log(fmt.Sprintf("WRITE %d %d", tx, value))
	s.wal.Sync()

	// Apply MVCC update
	for i := len(s.versions) - 1; i >= 0; i-- {
		v := s.versions[i]
		if v.beginTx <= tx && (v.endTx == 0 || v.endTx > tx) {
			v.endTx = tx
			break
		}
	}

	s.versions = append(s.versions, &Version{
		value:   value,
		beginTx: tx,
	})
}

func main() {
	store := NewStore()
	const N = 1_000

	for i := 0; i < N; i++ {
		tx := store.Begin()
		store.Read(tx)
		store.Write(tx, i)
	}

	fmt.Println("Go MVCC & WAL done:", store.Read(store.nextTx-1))
}
