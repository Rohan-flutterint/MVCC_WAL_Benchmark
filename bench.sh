#!/usr/bin/env bash

set -e

printf "\n"
echo "--------------------------"
go build -o mvcc_wal_go mvcc_wal.go
time ./mvcc_wal_go
echo "--------------------------"

printf "\n"
echo "--------------------------"
javac MVCC_WAL.java
time java MVCC_WAL
echo "--------------------------"

printf "\n"
echo "--------------------------"
g++ -O3 mvcc_wal.cpp -o mvcc_wal_cpp
time ./mvcc_wal_cpp
echo "--------------------------"

printf "\n"
echo "--------------------------"
rustc -O mvcc_wal.rs -o mvcc_wal_rust
time ./mvcc_wal_rust
echo "--------------------------"

# removing the generated binaries and class files
rm -f mvcc_wal_go mvcc_wal_cpp mvcc_wal_rust MVCC_WAL.class Version.class WAL.class wal_cpp.log wal_go.log wal_rust.log wal_java.log