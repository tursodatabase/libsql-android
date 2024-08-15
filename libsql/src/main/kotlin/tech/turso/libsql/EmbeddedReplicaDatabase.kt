package tech.turso.libsql

class EmbeddedReplicaDatabase(inner: Long) : Database(inner) {
    fun sync() {
        require(this.inner != 0L) { "Attempted to sync a closed Database" }
        nativeSync(this.inner)
    }

    private external fun nativeSync(db: Long)
}
