# MVCC Benchmark Playground

This repository is a **minimal implementation of MVCC (Multi-Version Concurrency Control)** in multiple languages, with a focus on **performance, memory behavior**.

The goal is **not** to build a production database, but to **understand how MVCC behaves under load** and how different language runtimes react to version churn.

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
- WAL
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
go build -o mvcc_go mvcc.go
./mvcc_go
```

### Rust
```sh
rustc -O mvcc.rs -o mvcc_rust
./mvcc_rust
```

### C++
```sh
g++ -O3 mvcc.cpp -o mvcc_cpp
./mvcc_cpp
```

### Java
```sh
javac MVCC.java
java MVCC
```


## RESULTS

```sh
-----------------------------
Go done: 49999999 Versions

real    0m1.948s
user    0m2.513s
sys     0m0.454s
-----------------------------

-----------------------------
Java done: 49999999 Versions

real    0m1.383s
user    0m3.959s
sys     0m0.864s
-----------------------------

-----------------------------
C++ done: 49999999 Versions

real    0m0.402s
user    0m0.115s
sys     0m0.086s
-----------------------------

-----------------------------
Rust done: 49999999 Versions

real    0m0.375s
user    0m0.121s
sys     0m0.038s
-----------------------------
```