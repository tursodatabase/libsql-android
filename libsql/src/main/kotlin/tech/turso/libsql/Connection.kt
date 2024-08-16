package tech.turso.libsql

interface Connection : AutoCloseable {
    fun execute(sql: String)

    fun executeBatch(sql: String)

    fun execute(
        sql: String,
        params: Map<String, Any?>,
    )

    fun execute(
        sql: String,
        vararg params: Any?,
    )

    fun query(sql: String): Rows

    fun query(
        sql: String,
        params: Map<String, Any?>,
    ): Rows

    fun query(
        sql: String,
        vararg params: Any?,
    ): Rows

    fun transaction(): Transaction

    override fun close()
}
