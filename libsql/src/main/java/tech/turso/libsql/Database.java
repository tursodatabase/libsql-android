package tech.turso.libsql;

public class Database implements AutoCloseable {

    protected long nativePtr;

    Database(long ptr) {
        if (ptr == 0) throw new RuntimeException("Attempted to construct a Database with a null pointer");
        nativePtr = ptr;
    }

    public Connection connect() {
        if (nativePtr == 0) return null;
        return new Connection(nativeConnect(nativePtr));
    }

    @Override
    public void close() {
        if (nativePtr != 0) {
            nativeClose(nativePtr);
            nativePtr = 0;
        }
    }

    private static native void nativeClose(long ptr);

    private static native long nativeConnect(long ptr);
}
