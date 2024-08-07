package tech.turso.libsql;

import android.util.Log;
import java.util.Map;

public class Connection implements AutoCloseable {

    private long nativePtr;

    Connection(long ptr) {
        nativePtr = ptr;
    }

    public void execute(String sql) throws Exception {
        Log.i("conn", "executing " + sql);
        if (nativePtr == 0) throw new Exception("Attempted to execute with null connection");
        nativeExecute(nativePtr, sql);
    }

    public void query(String sql) throws Exception {
        Log.i("conn", "querying " + sql);
        if (nativePtr == 0) throw new Exception("Attempted to query with null connection");
        nativeQuery(nativePtr, sql, new byte[] {});
    }

    public void query(String sql, Map<String, Value> params) throws Exception {
        var buf =
                Parameters.newBuilder()
                        .setNamed(NamedParameters.newBuilder().putAllParameters(params))
                        .build()
                        .toByteArray();
        Log.i("conn", "querying " + sql);
        if (nativePtr == 0) throw new Exception("Attempted to query with null connection");
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
