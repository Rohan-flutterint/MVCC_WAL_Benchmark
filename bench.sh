#!/usr/bin/env bash

set -e

printf "\n"
echo "----------------------"
go build -o mvcc_go mvcc.go
time ./mvcc_go
echo "----------------------"

printf "\n"
echo "----------------------"
javac MVCC.java
time java MVCC
echo "----------------------"

printf "\n"
echo "----------------------"
g++ -O3 mvcc.cpp -o mvcc_cpp
time ./mvcc_cpp
echo "----------------------"

printf "\n"
echo "----------------------"
rustc -O mvcc.rs -o mvcc_rust
time ./mvcc_rust
echo "----------------------"

# removing the generated binaries and class files
rm -f mvcc_go mvcc_cpp mvcc_rust MVCC.class Version.class