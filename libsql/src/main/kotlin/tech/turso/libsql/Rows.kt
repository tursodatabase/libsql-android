package tech.turso.libsql

import java.util.function.Consumer
import tech.turso.libsql.proto.Row as ProtoRow

typealias Row = List<Any?>

class Rows internal constructor(private var inner: Long) : AutoCloseable, Iterable<Row> {
    init {
        require(this.inner != 0L) { "Attempted to construct a Rows with a null pointer" }
    }

    val columnCount: Int
        get() = nativeColumnCount(inner)

    fun columnNames(idx: Int): String? {
        return nativeColumnName(this.inner, idx)
    }

    fun columnType(idx: Int): ValueType? {
        return ValueType.fromInt(nativeColumnType(this.inner ,idx))
    }

    fun next(): Row {
        val buf: ByteArray = nativeNext(this.inner)
        return ProtoRow.parseFrom(buf).valuesList.map {
            when {
                it.hasInteger() -> it.integer
                it.hasText() -> it.text
                it.hasReal() -> it.real
                it.hasBlob() -> it.blob.toByteArray()
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

    override fun iterator(): Iterator<Row> = RowsIterator(this)

    private external fun nativeColumnCount(rows: Long): Int

    private external fun nativeColumnName(rows: Long, idx: Int): String?

    private external fun nativeColumnType(rows: Long, idx: Int): Int

    private external fun nativeNext(rows: Long): ByteArray

    private external fun nativeClose(rows: Long)
}
