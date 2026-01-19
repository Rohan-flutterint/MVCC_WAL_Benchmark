package main

import "fmt"

type Version struct {
	value   int
	beginTx int
	endTx   int // 0 = infinity
}

type Store struct {
	versions []*Version
	nextTx   int
}

func NewStore() *Store {
	return &Store{
		versions: make([]*Version, 0, 1024),
		nextTx:   1,
	}
}

func (s *Store) Begin() int {
	tx := s.nextTx
	s.nextTx++
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
	const N = 50000000

	for i := 0; i < N; i++ {
		tx := store.Begin()
		store.Read(tx)
		store.Write(tx, i)
	}

	fmt.Println("Go done:", store.Read(store.nextTx-1))
}
