#!/usr/bin/env bash
set -e

echo "Running MVCC benchmarks in parallel (no core pinning)"
echo "===================================================="

# Ensure binaries exist
ls -l mvcc_go mvcc_rust mvcc_cpp MVCC.class > /dev/null

/usr/bin/time -p ./mvcc_go    > go.out   2> go.time   &
/usr/bin/time -p java -cp . MVCC > java.out 2> java.time &
/usr/bin/time -p ./mvcc_rust > rust.out 2> rust.time &
/usr/bin/time -p ./mvcc_cpp  > cpp.out  2> cpp.time  &

wait

echo
echo "================ Parallel Results ================"

echo "Go:"
cat go.time

echo
echo "Java:"
cat java.time

echo
echo "Rust:"
cat rust.time

echo
echo "C++:"
cat cpp.time
