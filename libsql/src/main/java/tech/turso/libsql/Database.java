package tech.turso.libsql;

public class Database implements AutoCloseable {

    protected long nativePtr;

    Database(long ptr) throws Exception {
        if (ptr == 0) throw new Exception("we are fucked");
        nativePtr = ptr;
    }

    public Connection connect() throws Exception {
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

    private static native long nativeConnect(long ptr) throws Exception;
}
