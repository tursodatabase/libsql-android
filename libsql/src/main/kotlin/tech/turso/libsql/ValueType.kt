package tech.turso.libsql

enum class ValueType(val value: Int) {
    Integer(1),
    Real(2),
    Text(3),
    Blob(4),
    Null(5);

    companion object {
        fun fromInt(value: Int): ValueType? {
            return entries.find { it.value == value }
        }
    }
}