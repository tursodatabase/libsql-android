package tech.turso.libsql

open class Database internal constructor(protected var inner: Long) : AutoCloseable {
    init {
        require(this.inner != 0L) { "Attempted to construct a Database with a null pointer" }
    }

    fun connect(): Connection {
        require(this.inner != 0L) { "Attempted to connect to a closed Database" }
        return ConnectionImpl(nativeConnect(this.inner))
    }

    override fun close() {
        require(this.inner != 0L) { "Database already closed" }
        nativeClose(this.inner)
        this.inner = 0L
    }

    private external fun nativeClose(db: Long)

    private external fun nativeConnect(db: Long): Long
}
