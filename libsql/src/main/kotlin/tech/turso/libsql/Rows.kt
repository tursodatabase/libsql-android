package tech.turso.libsql

class Rows internal constructor(private var inner: Long) : AutoCloseable {
    fun nextRow(): Array<Any> {
        return nativeNextRow(this.inner)
    }

    override fun close() {
        require(this.inner != 0L) { "Rows object already closed" }
        nativeClose(this.inner)
        this.inner = 0
    }

    private external fun nativeNextRow(rows: Long): Array<Any>

    private external fun nativeClose(rows: Long)
}
