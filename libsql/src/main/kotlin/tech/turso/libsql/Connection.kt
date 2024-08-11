package tech.turso.libsql

class Connection internal constructor(private var inner: Long) : AutoCloseable {
    fun execute(sql: String) {
        require(this.inner != 0L) { "Attempted to execute with a closed Connection" }
        nativeExecute(this.inner, sql, byteArrayOf())
    }

    fun execute(sql: String, params: Map<String, Value>) {
        require(this.inner != 0L) { "Attempted to execute with a closed Connection" }

        val buf =
            Parameters.newBuilder()
                .setNamed(NamedParameters.newBuilder().putAllParameters(params))
                .build()
                .toByteArray()

        nativeExecute(this.inner, sql, buf)
    }

    fun execute(
        sql: String,
        vararg params: Value,
    ) {
        require(this.inner != 0L) { "Attempted to execute with a closed Connection" }

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

    fun query(sql: String) {
        require(this.inner != 0L) { "Attempted to query with a closed Connection" }
        nativeQuery(this.inner, sql, byteArrayOf())
    }

    fun query(
        sql: String,
        params: Map<String, Value>,
    ) {
        require(this.inner != 0L) { "Attempted to query with a closed Connection" }

        val buf =
            Parameters.newBuilder()
                .setNamed(NamedParameters.newBuilder().putAllParameters(params))
                .build()
                .toByteArray()

        nativeQuery(this.inner, sql, buf)
    }

    fun query(
        sql: String,
        vararg params: Value,
    ) {
        require(this.inner != 0L) { "Attempted to query with a closed Connection" }

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

    private external fun nativeQuery(
        conn: Long,
        sql: String,
        buf: ByteArray,
    ): Long

    private external fun nativeClose(conn: Long)
}
