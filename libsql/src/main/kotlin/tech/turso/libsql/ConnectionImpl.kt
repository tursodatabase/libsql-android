package tech.turso.libsql

import tech.turso.libsql.proto.NamedParameters
import tech.turso.libsql.proto.Parameters
import tech.turso.libsql.proto.PositionalParameters
import tech.turso.libsql.proto.Value

open class ConnectionImpl internal constructor(private var inner: Long) : Connection {
    override fun executeBatch(sql: String) {
        require(this.inner != 0L) { "Attempted to batch execute with a closed Connection" }
        nativeExecuteBatch(this.inner, sql)
    }

    override fun execute(sql: String) {
        require(this.inner != 0L) { "Attempted to execute with a closed Connection" }
        nativeExecute(this.inner, sql, byteArrayOf())
    }

    override fun execute(
        sql: String,
        params: Map<String, Any?>,
    ) {
        require(this.inner != 0L) { "Attempted to execute with a closed Connection" }

        val params: Map<String, Value> = params.mapValues {
            when (val value = it.value) {
                is Int -> Value.newBuilder().setInteger(value.toLong()).build()
                is Long -> Value.newBuilder().setInteger(value).build()
                is String -> Value.newBuilder().setText(value).build()
                is Float -> Value.newBuilder().setReal(value.toDouble()).build()
                is Double-> Value.newBuilder().setReal(value).build()
                null -> Value.newBuilder().setNull(Value.Null.newBuilder().build()).build()
                else -> {
                    throw IllegalArgumentException("Type not supported")
                }
            }
        }

        val buf =
            Parameters.newBuilder()
                .setNamed(NamedParameters.newBuilder().putAllParameters(params))
                .build()
                .toByteArray()

        nativeExecute(this.inner, sql, buf)
    }

    override fun execute(
        sql: String,
        vararg params: Any?,
    ) {
        require(this.inner != 0L) { "Attempted to execute with a closed Connection" }

        val params = params.asList().map {
            when (val value = it) {
                is Int -> Value.newBuilder().setInteger(value.toLong()).build()
                is Long -> Value.newBuilder().setInteger(value).build()
                is String -> Value.newBuilder().setText(value).build()
                is Float -> Value.newBuilder().setReal(value.toDouble()).build()
                is Double-> Value.newBuilder().setReal(value).build()
                null -> Value.newBuilder().setNull(Value.Null.newBuilder().build()).build()
                else -> {
                    throw IllegalArgumentException("Type not supported")
                }
            }
        }

        val buf =
            Parameters.newBuilder()
                .setPositional(
                    PositionalParameters.newBuilder()
                        .addAllParameters(params),
                )
                .build()
                .toByteArray()

        nativeQuery(this.inner, sql, buf)
    }

    override fun query(sql: String): Rows {
        require(this.inner != 0L) { "Attempted to query with a closed Connection" }
        return Rows(nativeQuery(this.inner, sql, byteArrayOf()))
    }

    override fun query(
        sql: String,
        params: Map<String, Any?>,
    ): Rows {
        require(this.inner != 0L) { "Attempted to query with a closed Connection" }

        val params: Map<String, Value> = params.mapValues {
            when (val value = it.value) {
                is Int -> Value.newBuilder().setInteger(value.toLong()).build()
                is Long -> Value.newBuilder().setInteger(value).build()
                is String -> Value.newBuilder().setText(value).build()
                is Float -> Value.newBuilder().setReal(value.toDouble()).build()
                is Double-> Value.newBuilder().setReal(value).build()
                null -> Value.newBuilder().setNull(Value.Null.newBuilder().build()).build()
                else -> {
                    throw IllegalArgumentException("Type not supported")
                }
            }
        }

        val buf =
            Parameters.newBuilder()
                .setNamed(NamedParameters.newBuilder().putAllParameters(params))
                .build()
                .toByteArray()

        return Rows(nativeQuery(this.inner, sql, buf))
    }

    override fun query(
        sql: String,
        vararg params: Any?,
    ): Rows {
        require(this.inner != 0L) { "Attempted to query with a closed Connection" }

        val params = params.asList().map {
            when (val value = it) {
                is Int -> Value.newBuilder().setInteger(value.toLong()).build()
                is Long -> Value.newBuilder().setInteger(value).build()
                is String -> Value.newBuilder().setText(value).build()
                is Float -> Value.newBuilder().setReal(value.toDouble()).build()
                is Double-> Value.newBuilder().setReal(value).build()
                null -> Value.newBuilder().setNull(Value.Null.newBuilder().build()).build()
                else -> {
                    throw IllegalArgumentException("Type not supported")
                }
            }
        }

        val buf =
            Parameters.newBuilder()
                .setPositional(
                    PositionalParameters.newBuilder()
                        .addAllParameters(params),
                )
                .build()
                .toByteArray()

        return Rows(nativeQuery(this.inner, sql, buf))
    }

    override fun transaction(): Transaction {
        require(this.inner != 0L) { "Database already closed" }
        return Transaction(nativeTransaction(this.inner))
    }

    override fun close() {
        require(this.inner != 0L) { "Database already closed" }
        nativeClose(this.inner)
        this.inner = 0L
    }

    private external fun nativeExecute(
        conn: Long,
        sql: String,
        buf: ByteArray,
    )

    private external fun nativeExecuteBatch(
        conn: Long,
        sql: String,
    )

    private external fun nativeQuery(
        conn: Long,
        sql: String,
        buf: ByteArray,
    ): Long

    private external fun nativeTransaction(conn: Long): Long

    private external fun nativeClose(conn: Long)
}
