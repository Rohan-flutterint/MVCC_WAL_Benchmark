#include <vector>
#include <iostream>
#include <string>
#include <unistd.h>
#include <fcntl.h>

#ifdef __APPLE__
#include <sys/fcntl.h>
#endif

// MVCC
struct Version {
    int value;
    int beginTx;
    int endTx; // 0 = infinity
};

// WAL
class WAL {
    int fd;

public:
    WAL(const char* path) {
        fd = open(
            path,
            O_CREAT | O_APPEND | O_WRONLY,
            0644
        );
        if (fd < 0) {
            perror("open");
            _exit(1);
        }
    }

    void force_flush() {
#ifdef __APPLE__
        // Strongest possible flush on macOS
        if (fcntl(fd, F_FULLFSYNC) == -1) {
            perror("F_FULLFSYNC");
            _exit(1);
        }
#else
        // Linux / others
        if (fsync(fd) == -1) {
            perror("fsync");
            _exit(1);
        }
#endif
    }

    void log(const std::string& record) {
        write(fd, record.c_str(), record.size());
        write(fd, "\n", 1);
        force_flush();  // FORCE DURABILITY
    }
};

// Store
class Store {
    std::vector<Version> versions;
    int nextTx = 1;
    WAL wal;

public:
    Store() : wal("wal_cpp.log") {
        versions.reserve(1024);
    }

    int begin() {
        int tx = nextTx++;
        wal.log("BEGIN " + std::to_string(tx));
        return tx;
    }

    int read(int tx) {
        for (int i = versions.size() - 1; i >= 0; i--) {
            auto &v = versions[i];
            if (v.beginTx <= tx && (v.endTx == 0 || v.endTx > tx)) {
                return v.value;
            }
        }
        return 0;
    }

    void write(int tx, int value) {
        wal.log("WRITE " + std::to_string(tx) + " " + std::to_string(value));

        for (int i = versions.size() - 1; i >= 0; i--) {
            auto &v = versions[i];
            if (v.beginTx <= tx && (v.endTx == 0 || v.endTx > tx)) {
                v.endTx = tx;
                break;
            }
        }

        versions.push_back({value, tx, 0});
    }
};


int main() {
    Store store;
    const int N = 1000;

    for (int i = 0; i < N; i++) {
        int tx = store.begin();
        store.read(tx);
        store.write(tx, i);
    }

    std::cout << "C++ MVCC & WAL done: "
              << store.read(store.read(N)) << "\n";
}
