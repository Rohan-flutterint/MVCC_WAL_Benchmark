struct Version {
    value: i32,
    begin_tx: i32,
    end_tx: i32, // 0 = infinity
}

struct Store {
    versions: Vec<Version>,
    next_tx: i32,
}

impl Store {
    fn new() -> Self {
        Store {
            versions: Vec::with_capacity(1024),
            next_tx: 1,
        }
    }

    fn begin(&mut self) -> i32 {
        let tx = self.next_tx;
        self.next_tx += 1;
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
    const N: i32 = 50000000;

    for i in 0..N {
        let tx = store.begin();
        store.read(tx);
        store.write(tx, i);
    }

    println!("Rust done: {}", store.read(store.next_tx - 1));
}