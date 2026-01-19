# MVCC Benchmark Playground

This repository is a **minimal implementation of MVCC (Multi-Version Concurrency Control) & WAL (Write Ahead Log)** in multiple languages, with a focus on **performance, memory behavior**.

The goal is **not** to build a production database, but to **understand how MVCC & WAL behaves under load** and how different language runtimes react to version churn.

---

## What is implemented

- Simple in-memory MVCC model
- Append-only version chains
- Snapshot-based visibility rules
- Microbenchmarks for:
  - Go
  - Java
  - Rust
  - C++

This project intentionally avoids:
- Indexes
- Locks
- Disk IO
- Concurrency

So the results reflect **runtime + memory model behavior**, not database features.

---

## Why this exists

MVCC performance is dominated by:
- Memory allocation
- Version retention
- Garbage collection vs manual memory
- Cache locality

These benchmarks demonstrate:
- Why unbounded MVCC history is a memory leak by design
- Why vacuum is mandatory in real databases
- Why GC-heavy runtimes struggle under version churn
- Why Rust/C++ excel for storage engines

---

## Files

| File | Description |
|-----|------------|
| `mvcc.go` | Basic MVCC in Go |
| `MVCC.java` | Java MVCC |
| `mvcc.rs` | Rust MVCC |
| `mvcc.cpp` | C++ MVCC |
| `bench.sh` | Sequential benchmark |
| `bench_parallel.sh` | Parallel benchmark (no core pinning) |

---

## Build

Run the Shell Script which will generate the binaries and carry out benchmark (Recommended)

```sh
chmod +x bench.sh
./bench
```

(OR)

Build binaries explicitly before benchmarking.

### Go
```sh
go build -o mvcc_wal_go mvcc_wal.go
./mvcc_wal_go
```

### Rust
```sh
rustc -O mvcc_wal.rs -o mvcc_wal_rust
./mvcc_wal_rust
```

### C++
```sh
g++ -O3 mvcc_wal.cpp -o mvcc_wal_cpp
./mvcc_cpp
```

### Java
```sh
javac MVCC_WAL.java
java MVCC_WAL
```


## RESULTS

```sh
--------------------------
Go MVCC & WAL done: 999

real    0m5.375s
user    0m0.027s
sys     0m0.331s
--------------------------

--------------------------
Java MVCC & WAL done: 999

real    0m5.360s
user    0m0.171s
sys     0m0.334s
--------------------------

--------------------------
C++ MVCC & WAL done: 998

real    0m5.455s
user    0m0.007s
sys     0m0.341s
--------------------------

--------------------------
Rust MVCC & WAL done: 999

real    0m5.257s
user    0m0.012s
sys     0m0.366s
--------------------------
```