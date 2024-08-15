package tech.turso.libsql

import tech.turso.libsql.proto.Row
import tech.turso.libsql.proto.Value

class Rows internal constructor(private var inner: Long) : AutoCloseable {
    init {
        require(this.inner != 0L) { "Attempted to construct a Rows with a null pointer" }
    }

    fun next(): List<Value> {
        val buf: ByteArray = nativeNext(this.inner)
        return Row.parseFrom(buf).valuesList
    }

    override fun close() {
        require(this.inner != 0L) { "Rows object already closed" }
        nativeClose(this.inner)
        this.inner = 0
    }

    private external fun nativeNext(rows: Long): ByteArray

    private external fun nativeClose(rows: Long)
}
