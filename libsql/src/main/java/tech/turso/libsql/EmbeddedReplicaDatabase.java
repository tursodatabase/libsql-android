package tech.turso.libsql;

public class EmbeddedReplicaDatabase extends Database {
    EmbeddedReplicaDatabase(long ptr) throws Exception {
        super(ptr);
    }

    public void sync() {
        nativeSync(nativePtr);
    }

    private static native void nativeSync(long ptr);
}
