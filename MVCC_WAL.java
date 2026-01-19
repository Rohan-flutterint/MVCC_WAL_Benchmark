import java.io.*;
import java.nio.channels.FileChannel;
import java.util.*;

// MVCC
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

// WAL
class WAL {
    private final FileChannel channel;

    WAL(String path) {
        try {
            FileOutputStream fos = new FileOutputStream(path, true);
            this.channel = fos.getChannel();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    void log(String record) {
        try {
            byte[] bytes = (record + "\n").getBytes();
            channel.write(java.nio.ByteBuffer.wrap(bytes));
            channel.force(true);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    void sync() {
        try {
            channel.force(true);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

// Store
public class MVCC_WAL {
    List<Version> versions = new ArrayList<>();
    int nextTx = 1;
    WAL wal = new WAL("wal_java.log");

    int begin() {
        int tx = nextTx++;

        // WAL: BEGIN
        wal.log("BEGIN " + tx);
        wal.sync();

        return tx;
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
        // WAL: WRITE
        wal.log("WRITE " + tx + " " + value);
        wal.sync();

        // Apply MVCC update
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
        MVCC_WAL store = new MVCC_WAL();
        final int N = 1_000;

        for (int i = 0; i < N; i++) {
            int tx = store.begin();
            store.read(tx);
            store.write(tx, i);
        }

        System.out.println("Java MVCC & WAL done: " + store.read(store.nextTx - 1));
    }
}
