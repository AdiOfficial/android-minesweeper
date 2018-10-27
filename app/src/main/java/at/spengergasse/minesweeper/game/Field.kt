package at.spengergasse.minesweeper.game

import kotlin.experimental.and
import kotlin.experimental.inv
import kotlin.experimental.or

class Field private constructor(private val data: ByteArray, mines: Int, val rows: Int, val columns: Int) {

    constructor(rows: Int, columns: Int) : this(ByteArray(Math.ceil(rows * columns / 8.0).toInt()) { 0 }, 0, rows, columns)
    constructor(other: Field) : this(other.data.copyOf(), other.mines, other.rows, other.columns)

    var mines: Int = mines
        private set

    companion object {
        @JvmStatic
        fun createSampleMedium() = Field(16, 16).apply {
            this[0, 1] = true
            this[0, 14] = true
            this[1, 4] = true
            this[1, 12] = true
            this[3, 1] = true
            this[3, 12] = true
            this[4, 12] = true
            this[5, 1] = true
            this[6, 3] = true
            this[6, 7] = true
            this[6, 12] = true
            this[6, 14] = true
            this[7, 0] = true
            this[7, 1] = true
            this[7, 7] = true
            this[7, 9] = true
            this[7, 13] = true
            this[8, 6] = true
            this[8, 7] = true
            this[8, 11] = true
            this[8, 13] = true
            this[9, 5] = true
            this[9, 7] = true
            this[10, 10] = true
            this[11, 1] = true
            this[11, 7] = true
            this[11, 15] = true
            this[12, 1] = true
            this[12, 8] = true
            this[13, 4] = true
            this[13, 10] = true
            this[13, 11] = true
            this[13, 14] = true
            this[14, 2] = true
            this[14, 7] = true
            this[14, 15] = true
            this[15, 5] = true
            this[15, 7] = true
            this[15, 9] = true
            this[15, 14] = true
        }
    }

    private val dataLock = Any()

    val fields = rows * columns

    operator fun set(row: Int, column: Int, mine: Boolean) {
        if (row !in 0 until rows) throw ArrayIndexOutOfBoundsException(row)
        if (column !in 0 until columns) throw ArrayIndexOutOfBoundsException(column)

        val whichField = columns * row + column
        val whichByte = whichField ushr 3
        val whichBit = whichField % 8

        synchronized(dataLock) {
            val oldValue = data[whichByte]
            val newValue = if (mine) oldValue or (1 shl whichBit).toByte() else oldValue and (1 shl whichBit).toByte().inv()
            if (oldValue == newValue) return
            data[whichByte] = newValue
            if (mine) mines++ else mines--
        }
    }

    private fun unsynchronizedGetInt(row: Int, column: Int): Int {
        if (row !in 0 until rows || column !in 0 until columns) return 0
        return if (unsynchronizedUncheckedGet(row, column)) 1 else 0
    }

    fun getAdjacent(row: Int, column: Int): Int {
        if (row !in 0 until rows) throw ArrayIndexOutOfBoundsException(row)
        if (column !in 0 until columns) throw ArrayIndexOutOfBoundsException(column)

        synchronized(dataLock) {
            return 0 +
                    unsynchronizedGetInt(row, column - 1) +
                    unsynchronizedGetInt(row - 1, column - 1) +
                    unsynchronizedGetInt(row - 1, column) +
                    unsynchronizedGetInt(row - 1, column + 1) +
                    unsynchronizedGetInt(row, column + 1) +
                    unsynchronizedGetInt(row + 1, column + 1) +
                    unsynchronizedGetInt(row + 1, column) +
                    unsynchronizedGetInt(row + 1, column - 1)
        }
    }

    private fun unsynchronizedUncheckedGet(row: Int, column: Int): Boolean {
        val whichField = columns * row + column
        val whichByte = whichField ushr 3
        val whichBit = whichField % 8

        return data[whichByte].toInt() and 0b11111111 ushr whichBit and 1 == 1
    }

    private fun uncheckedGet(row: Int, column: Int): Boolean {
        val whichField = columns * row + column
        val whichByte = whichField ushr 3
        val whichBit = whichField % 8

        synchronized(dataLock) {
            return data[whichByte].toInt() and 0b11111111 ushr whichBit and 1 == 1
        }
    }

    operator fun get(row: Int, column: Int): Boolean {
        if (row !in 0 until rows) throw ArrayIndexOutOfBoundsException(row)
        if (column !in 0 until columns) throw ArrayIndexOutOfBoundsException(column)

        return uncheckedGet(row, column)
    }

}