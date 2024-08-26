package tech.turso.libsql

class RowsIterator(val rows: Rows): Iterator<Row> {
    private var currentRow: Row = rows.next()

    override fun hasNext(): Boolean = currentRow.isNotEmpty()

    override fun next(): Row {
        val previousRow = this.currentRow
        this.currentRow = this.rows.next()
        return previousRow
    }
}