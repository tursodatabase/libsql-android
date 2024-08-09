package tech.turso.libsql;

import android.util.Log;
import java.util.Arrays;
import java.util.Map;

public class Connection implements AutoCloseable {

    private long nativePtr;

    Connection(long ptr) {
        nativePtr = ptr;
    }

    public void execute(String sql) {
        if (nativePtr == 0) throw new RuntimeException("Attempted to execute a null connection");
        Log.i("conn", "executing " + sql);

        nativeExecute(nativePtr, sql);
    }

    public void query(String sql) {
        if (nativePtr == 0) throw new RuntimeException("Attempted to query a null connection");
        Log.i("conn", "querying " + sql);

        nativeQuery(nativePtr, sql, new byte[] {});
    }

    public void query(String sql, Map<String, Value> params) {
        if (nativePtr == 0) throw new RuntimeException("Attempted to query a null connection");
        Log.i("conn", "querying " + sql);

        var buf =
                Parameters.newBuilder()
                        .setNamed(NamedParameters.newBuilder().putAllParameters(params))
                        .build()
                        .toByteArray();

        nativeQuery(nativePtr, sql, buf);
    }

    public void query(String sql, Value... params) {
        if (nativePtr == 0) throw new RuntimeException("Attempted to query a null connection");
        Log.i("conn", "querying " + sql);

        var buf =
                Parameters.newBuilder()
                        .setPositional(
                                PositionalParameters.newBuilder()
                                        .addAllParameters(Arrays.asList(params)))
                        .build()
                        .toByteArray();

        nativeQuery(nativePtr, sql, buf);
    }

    @Override
    public void close() {
        if (nativePtr != 0) {
            nativeClose(nativePtr);
            nativePtr = 0;
        }
    }

    private static native void nativeExecute(long ptr, String sql);

    private static native long nativeQuery(long ptr, String sql, byte[] buf);

    private static native void nativeClose(long ptr);
}
