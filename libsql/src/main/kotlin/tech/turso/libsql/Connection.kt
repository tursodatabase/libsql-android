package tech.turso.libsql

import tech.turso.libsql.proto.Value

interface Connection : AutoCloseable {
    fun execute(sql: String)

    fun executeBatch(sql: String)

    fun executeBatch(sql: List<String>) {
        executeBatch(sql.joinToString("; "))
    }

    fun execute(
        sql: String,
        params: Map<String, Value>,
    )

    fun execute(
        sql: String,
        vararg params: Value,
    )

    fun query(sql: String): Rows

    fun query(
        sql: String,
        params: Map<String, Value>,
    ): Rows

    fun query(
        sql: String,
        vararg params: Value,
    ): Rows

    fun transaction(): Transaction

    override fun close()
}
