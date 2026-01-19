#include <vector>
#include <iostream>

struct Version {
    int value;
    int beginTx;
    int endTx; // 0 = infinity
};

class Store {
    std::vector<Version> versions;
    int nextTx = 1;

public:
    Store() {
        versions.reserve(1024);
    }

    int begin() {
        return nextTx++;
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
    const int N = 50000000;

    for (int i = 0; i < N; i++) {
        int tx = store.begin();
        store.read(tx);
        store.write(tx, i);
    }

    std::cout << "C++ done: " << store.read(N) << "\n";
}