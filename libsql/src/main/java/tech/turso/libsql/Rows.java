package tech.turso.libsql;

public class Rows implements AutoCloseable {

    private long nativePtr;

    Rows(long ptr) {
        nativePtr = ptr;
    }

    public Object[] nextRow() {
        return nativeNextRow(nativePtr);
    }

    @Override
    public void close() {
        if (nativePtr != 0) {
            nativeClose(nativePtr);
            nativePtr = 0;
        }
    }

    private static native Object[] nativeNextRow(long ptr);

    private static native void nativeClose(long ptr);
}
