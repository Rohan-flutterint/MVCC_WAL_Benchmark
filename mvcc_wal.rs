use std::fs::{OpenOptions, File};
use std::io::Write;

// MVCC
struct Version {
    value: i32,
    begin_tx: i32,
    end_tx: i32, // 0 = infinity
}

// WAL
struct WAL {
    file: File,
}

impl WAL {
    fn new(path: &str) -> Self {
        let file = OpenOptions::new()
            .create(true)
            .append(true)
            .write(true)
            .open(path)
            .unwrap();
        WAL { file }
    }

    fn log(&mut self, record: &str) {
        writeln!(self.file, "{}", record).unwrap();
        self.file.sync_all().unwrap(); // fsync equivalent
    }
}

// Store
struct Store {
    versions: Vec<Version>,
    next_tx: i32,
    wal: WAL,
}

impl Store {
    fn new() -> Self {
        Store {
            versions: Vec::with_capacity(1024),
            next_tx: 1,
            wal: WAL::new("wal_rust.log"),
        }
    }

    fn begin(&mut self) -> i32 {
        let tx = self.next_tx;
        self.next_tx += 1;

        // WAL: BEGIN
        self.wal.log(&format!("BEGIN {}", tx));

        tx
    }

    fn read(&self, tx: i32) -> i32 {
        for v in self.versions.iter().rev() {
            if v.begin_tx <= tx && (v.end_tx == 0 || v.end_tx > tx) {
                return v.value;
            }
        }
        0
    }

    fn write(&mut self, tx: i32, value: i32) {
        // WAL: WRITE
        self.wal.log(&format!("WRITE {} {}", tx, value));

        // Apply MVCC update
        for v in self.versions.iter_mut().rev() {
            if v.begin_tx <= tx && (v.end_tx == 0 || v.end_tx > tx) {
                v.end_tx = tx;
                break;
            }
        }

        self.versions.push(Version {
            value,
            begin_tx: tx,
            end_tx: 0,
        });
    }
}

fn main() {
    let mut store = Store::new();
    const N: i32 = 1_000;

    for i in 0..N {
        let tx = store.begin();
        store.read(tx);
        store.write(tx, i);
    }

    println!("Rust MVCC & WAL done: {}", store.read(store.next_tx - 1));
}
