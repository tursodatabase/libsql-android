package tech.turso.libsql

object Libsql {
    init {
        System.loadLibrary("libsql_android")
    }

    @JvmStatic
    fun open(path: String): Database {
        return Database(nativeOpenLocal(path))
    }

    @JvmStatic
    fun open(
        url: String,
        authToken: String,
    ): Database {
        return Database(nativeOpenRemote(url, authToken))
    }

    @JvmStatic
    fun open(
        path: String,
        url: String,
        authToken: String,
    ): EmbeddedReplicaDatabase {
        return EmbeddedReplicaDatabase(nativeOpenEmbeddedReplica(path, url, authToken))
    }

    private external fun nativeOpenLocal(path: String): Long

    private external fun nativeOpenRemote(
        url: String,
        authToken: String,
    ): Long

    private external fun nativeOpenEmbeddedReplica(
        path: String,
        url: String,
        authToken: String,
    ): Long
}
