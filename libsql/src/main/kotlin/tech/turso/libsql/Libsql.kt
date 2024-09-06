package tech.turso.libsql

import com.google.protobuf.ByteString
import tech.turso.libsql.proto.Value

fun Any?.toValue(): Value = when (this) {
    is Int -> Value.newBuilder().setInteger(this.toLong()).build()
    is Long -> Value.newBuilder().setInteger(this).build()
    is String -> Value.newBuilder().setText(this).build()
    is Float -> Value.newBuilder().setReal(this.toDouble()).build()
    is Double -> Value.newBuilder().setReal(this).build()
    is ByteArray -> Value.newBuilder().setBlob(ByteString.copyFrom(this)).build()
    null -> Value.newBuilder().setNull(Value.Null.newBuilder().build()).build()
    else -> {
        throw IllegalArgumentException("${this::class.simpleName} not supported")
    }
}

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
        syncInterval: Long = 0,
        readYourWrites: Boolean = true,
        withWebpki: Boolean = true,
    ): EmbeddedReplicaDatabase {
        return EmbeddedReplicaDatabase(
            nativeOpenEmbeddedReplica(
                path,
                url,
                authToken,
                syncInterval,
                readYourWrites,
                withWebpki
            )
        )
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
        syncInterval: Long,
        readYourWrites: Boolean,
        withWebpki: Boolean,
    ): Long
}
