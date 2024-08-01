package tech.turso.libsql;

public class Connection implements AutoCloseable {

    private long nativePtr;

    Connection(long ptr) {
        nativePtr = ptr;
    }

    public void execute(String sql) {
        nativeExecute(nativePtr, sql);
    }

    public Rows query(String sql) {
        return new Rows(nativeQuery(nativePtr, sql));
    }

    @Override
    public void close() {
        if (nativePtr != 0) {
            nativeClose(nativePtr);
            nativePtr = 0;
        }
    }

    private static native void nativeExecute(long ptr, String sql);
    private static native long nativeQuery(long ptr, String sql);
    private static native void nativeClose(long ptr);
}
