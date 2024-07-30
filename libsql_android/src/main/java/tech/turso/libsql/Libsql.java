package tech.turso.libsql;

public class Libsql {

    // Used to load the 'libsql' library on application startup.
    static {
        System.loadLibrary("libsql_android");
    }

    public static Database openLocal(String dbFile) {
        return new Database(nativeOpenLocal(dbFile));
    }
    private static native long nativeOpenLocal(String dbFile);

    public static Database openRemote(String url, String authToken) {
        return new Database(nativeOpenRemote(url, authToken));
    }
    private static native long nativeOpenRemote(String url, String authToken);

    public static EmbeddedReplicaDatabase openEmbeddedReplica(String dbFile, String url, String authToken) {
        return new EmbeddedReplicaDatabase(nativeOpenEmbeddedReplica(dbFile, url, authToken));
    }
    private static native long nativeOpenEmbeddedReplica(String dbFile, String url, String authToken);
}