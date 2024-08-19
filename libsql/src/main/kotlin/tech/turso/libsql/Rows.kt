package tech.turso.libsql

import tech.turso.libsql.proto.Row

class Rows internal constructor(private var inner: Long) : AutoCloseable {
    init {
        require(this.inner != 0L) { "Attempted to construct a Rows with a null pointer" }
    }

    fun next(): List<Any?> {
        val buf: ByteArray = nativeNext(this.inner)
        return Row.parseFrom(buf).valuesList.map {
            when {
                it.hasInteger() -> it.integer
                it.hasText() -> it.text
                it.hasReal() -> it.real
                it.hasNull() -> null
                else -> RuntimeException("Invalid Row")
            }
        }
    }

    override fun close() {
        require(this.inner != 0L) { "Rows object already closed" }
        nativeClose(this.inner)
        this.inner = 0
    }

    private external fun nativeNext(rows: Long): ByteArray

    private external fun nativeClose(rows: Long)
}
