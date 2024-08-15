
package tech.turso.libsql

import tech.turso.libsql.proto.NamedParameters
import tech.turso.libsql.proto.Parameters
import tech.turso.libsql.proto.PositionalParameters
import tech.turso.libsql.proto.Value

class Transaction internal constructor(private var inner: Long) : Connection {
    override fun execute(sql: String) {
        require(this.inner != 0L) { "Attempted to execute with a closed Transaction" }
        nativeExecute(this.inner, sql, byteArrayOf())
    }

    override fun execute(sql: String, params: Map<String, Value>) {
        require(this.inner != 0L) { "Attempted to execute with a closed Transaction" }

        val buf =
            Parameters.newBuilder()
                .setNamed(NamedParameters.newBuilder().putAllParameters(params))
                .build()
                .toByteArray()

        nativeExecute(this.inner, sql, buf)
    }

    override fun execute(
        sql: String,
        vararg params: Value,
    ) {
        require(this.inner != 0L) { "Attempted to execute with a closed Transaction" }

        val buf =
            Parameters.newBuilder()
                .setPositional(
                    PositionalParameters.newBuilder()
                        .addAllParameters(params.asList()),
                )
                .build()
                .toByteArray()

        nativeQuery(this.inner, sql, buf)
    }

    override fun query(sql: String): Rows {
        require(this.inner != 0L) { "Attempted to query with a closed Transaction" }
        return Rows(nativeQuery(this.inner, sql, byteArrayOf()))
    }

    override fun query(
        sql: String,
        params: Map<String, Value>,
    ): Rows {
        require(this.inner != 0L) { "Attempted to query with a closed Transaction" }

        val buf =
            Parameters.newBuilder()
                .setNamed(NamedParameters.newBuilder().putAllParameters(params))
                .build()
                .toByteArray()

        return Rows(nativeQuery(this.inner, sql, buf))
    }

    override fun query(
        sql: String,
        vararg params: Value,
    ): Rows {
        require(this.inner != 0L) { "Attempted to query with a closed Transaction" }

        val buf =
            Parameters.newBuilder()
                .setPositional(
                    PositionalParameters.newBuilder()
                        .addAllParameters(params.asList()),
                )
                .build()
                .toByteArray()

        return Rows(nativeQuery(this.inner, sql, buf))
    }

    override fun transaction(): Transaction {
        require(this.inner != 0L) { "Transaction already closed" }
        return Transaction(nativeTransaction(this.inner))
    }

    override fun close() {
        require(this.inner != 0L) { "Transaction already closed" }
        nativeClose(this.inner)
        this.inner = 0L
    }

    private external fun nativeExecute(
        conn: Long,
        sql: String,
        buf: ByteArray,
    )

    private external fun nativeQuery(
        conn: Long,
        sql: String,
        buf: ByteArray,
    ): Long

    private external fun nativeTransaction(conn: Long): Long

    private external fun nativeClose(conn: Long)
}
