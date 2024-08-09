package tech.turso.libsql

class Database internal constructor(private var inner: Long) : AutoCloseable {
    init {
        require(this.inner != 0L) { "Attempted to construct a Database with a null pointer" }
    }

    fun connect(): Connection {
        require(this.inner != 0L) { "Attempted to connect to a closed Database" }
        return Connection(nativeConnect(this.inner))
    }

    fun sync() {
        require(this.inner != 0L) { "Attempted to sync a closed Database" }
        nativeSync(this.inner)
    }

    override fun close() {
        require(this.inner != 0L) { "Database already closed" }
        nativeClose(this.inner)
        this.inner = 0L
    }

    private external fun nativeSync(db: Long)

    private external fun nativeClose(db: Long)

    private external fun nativeConnect(db: Long): Long
}
