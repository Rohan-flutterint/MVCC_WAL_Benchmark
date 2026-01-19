import java.util.*;

class Version {
    int value;
    int beginTx;
    int endTx; // 0 = infinity

    Version(int value, int beginTx) {
        this.value = value;
        this.beginTx = beginTx;
        this.endTx = 0;
    }
}

public class MVCC {
    List<Version> versions = new ArrayList<>();
    int nextTx = 1;

    int begin() {
        return nextTx++;
    }

    int read(int tx) {
        for (int i = versions.size() - 1; i >= 0; i--) {
            Version v = versions.get(i);
            if (v.beginTx <= tx && (v.endTx == 0 || v.endTx > tx)) {
                return v.value;
            }
        }
        return 0;
    }

    void write(int tx, int value) {
        for (int i = versions.size() - 1; i >= 0; i--) {
            Version v = versions.get(i);
            if (v.beginTx <= tx && (v.endTx == 0 || v.endTx > tx)) {
                v.endTx = tx;
                break;
            }
        }
        versions.add(new Version(value, tx));
    }

    public static void main(String[] args) {
        MVCC store = new MVCC();
        final int N = 50000000;

        for (int i = 0; i < N; i++) {
            int tx = store.begin();
            store.read(tx);
            store.write(tx, i);
        }

        System.out.println("Java done: " + store.read(store.nextTx - 1));
    }
}