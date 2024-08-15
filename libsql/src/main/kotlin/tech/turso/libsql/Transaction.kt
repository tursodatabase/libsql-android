import tech.turso.libsql.Connection
import tech.turso.libsql.Rows
import tech.turso.libsql.proto.Value

class Transaction internal constructor(private var inner: Long) : Connection {
    override fun execute(sql: String) {
        TODO("Not yet implemented")
    }

    override fun execute(sql: String, params: Map<String, Value>) {
        TODO("Not yet implemented")
    }

    override fun execute(sql: String, vararg params: Value) {
        TODO("Not yet implemented")
    }

    override fun query(sql: String): Rows {
        TODO("Not yet implemented")
    }

    override fun query(sql: String, params: Map<String, Value>): Rows {
        TODO("Not yet implemented")
    }

    override fun query(sql: String, vararg params: Value): Rows {
        TODO("Not yet implemented")
    }

    override fun transaction(): Transaction {
        TODO("Not yet implemented")
    }

    override fun close() {
        TODO("Not yet implemented")
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
