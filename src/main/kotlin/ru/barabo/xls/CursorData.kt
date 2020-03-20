package ru.barabo.xls

import ru.barabo.db.Query
import ru.barabo.db.SessionSetting

class CursorData(private val querySession: QuerySession, private val querySelect: String,
                 val params: List<ReturnResult> = emptyList() ) {

    var data: List<Array<Any?>> = emptyList()

    private var row: Int = 0

    private var columns: List<String> = emptyList()

    private var sqlColumnType: List<Int> = emptyList()

    private var isOpen: Boolean = false

    private val columnResult = ArrayList<ColumnResult>()

    fun getColumnResult(columnName: String): ReturnResult {
        return columnResult.firstOrNull { it.columnName == columnName }
            ?: ColumnResult(this, columnName).apply { columnResult += this }
    }

    fun reInitRow() {
        row = 0
    }

    fun isNext(): Boolean {
        return if(row + 1 < data.size) {
            row++

            true
        } else false
    }

    fun isEmpty(): Boolean {
        if(!isOpen) {
            isOpen = open()
        }
        return data.isEmpty()
    }

    fun toSqlValue(index: Int, funIndex: Int): Any {
        if(!isOpen) {
            isOpen = open()
        }

        if(index < 0 || funIndex != UNINITIALIZE_COLUMN_INDEX) {
            return funCursorValue(index, funIndex)
        }

        if(row >= data.size) throw Exception("cursor position is end")

        return data[row][index] ?: sqlColumnType[index].toSqlValueNull()
    }

    fun getVarResult(index: Int, funIndex: Int = UNINITIALIZE_COLUMN_INDEX): VarResult {
        if(!isOpen) {
            isOpen = open()
        }

        if(index < 0 || funIndex != UNINITIALIZE_COLUMN_INDEX) {
            return funCursorValue(index, funIndex)
        }

        if(row >= data.size) throw Exception("cursor position is end")

        val value = data[row][index]

        val type = VarType.varTypeBySqlType(sqlColumnType[index])

        return VarResult(type = type, value = value)
    }

    private fun funCursorValue(index: Int, funIndex: Int): VarResult {

        val findIndex = if(index < 0) index else funIndex

        val cursorFun = CursorFun.byIndex(findIndex) ?: throw Exception("cursor fun is not found index=$index")

        return cursorFun.func.invoke(this, index)
    }

    internal fun getColumnIndex(columnName: String): Pair<Int, Int> {
        if(!isOpen) {
            isOpen = open()
        }

        val column = columnName.substringBefore('.')

        val funColumn = columnName.substringAfter('.', "")

        val columnIndex = columnIndexByName(column)

        val funIndex = if(funColumn.isBlank()) UNINITIALIZE_COLUMN_INDEX else columnIndexByName(funColumn)

        return Pair(columnIndex, funIndex)
    }

    private fun columnIndexByName(columnName: String): Int {
        return CursorFun.byColumn(columnName)?.index ?:
        columns.withIndex().firstOrNull { it.value.equals(columnName, true) }?.index ?:
        throw Exception("not found column for cursor .$columnName")
    }

    private fun open(): Boolean {

        val param: Array<Any?>? = if(params.isEmpty()) null else params.map { it.getSqlValue() }.toTypedArray()

        val allData = if(isCursor()) {
            querySession.query.selectCursorWithMetaData(querySelect, param, querySession.sessionSetting)
        } else {
            querySession.query.selectWithMetaData(querySelect, param, querySession.sessionSetting)
        }

        data = allData.data
        columns = allData.columns
        sqlColumnType = allData.types
        row = 0

        return true
    }

    fun findRowByRecord(record: Record): Int? {

        val indexColumnFind = columns.indices.firstOrNull {
            record.columns[it].result.type != VarType.UNDEFINED &&
            record.columns[it].result.value != null
        } ?: return null

        val findValue = record.columns[indexColumnFind].result.value!!
        val findType = record.columns[indexColumnFind].result.type

        return data.indices.firstOrNull {
            data[it][indexColumnFind]  != null && (
                    data[it][indexColumnFind] == findValue ||
                    findType.isEqualVal(findValue, data[it][indexColumnFind]!! )
            )
        }
    }

    fun setRecordByRow(record: Record, rowIndex: Int) {
        if(rowIndex < 0 || rowIndex >= data.size) {
            record.clearData()
            return
        }

        for(index in columns.indices) {
            record.columns[index].result.type = VarType.varTypeBySqlType(sqlColumnType[index])
            record.columns[index].result.value = data[rowIndex][index]
        }
    }

    fun record(columnIndex: Int): VarResult {

        val columnsRecord = columns.withIndex().map { Var(it.value , getVarResult(it.index)) }.toMutableList()

        return VarResult(VarType.RECORD, Record(columnsRecord))
    }

    fun emptyRecord(columnIndex: Int) = VarResult(VarType.RECORD, Record(columns.map { Var(it, VarResult()) }.toMutableList()) )

    private fun isCursor() = querySelect[0] == '{'

    fun row(columnIndex: Int) = VarResult(VarType.INT, row + 1)

    fun sum(columnIndex: Int) = VarResult(VarType.NUMBER, data.sumByDouble { (it[columnIndex] as? Number)?.toDouble()?:0.0 })

    fun max(columnIndex: Int) = VarResult(VarType.NUMBER, data.map { (it[columnIndex] as? Number)?.toDouble()?:0.0 }.max()?:0.0)

    fun min(columnIndex: Int) = VarResult(VarType.NUMBER,data.map { (it[columnIndex] as? Number)?.toDouble()?:0.0 }.min()?:0.0)

    fun count(columnIndex: Int) = VarResult(VarType.INT, data.size)

    fun isEmptyFun(columnIndex: Int) = VarResult(VarType.INT, if(isEmpty()) 1 else 0)

    fun isNotEmptyFun(columnIndex: Int) = VarResult(VarType.INT, if(isEmpty()) 0 else 1)
}

private enum class CursorFun(val index: Int, val funName: String, val func: CursorData.(columnIndex: Int) -> VarResult ) {
    ROW(-1, "ROW", CursorData::row),
    SUM(-2, "SUM", CursorData::sum),
    COUNT(-3, "COUNT", CursorData::count),
    ISEMPTY(-4, "ISEMPTY", CursorData::isEmptyFun),
    ISNOTEMPTY(-5, "ISNOTEMPTY", CursorData::isNotEmptyFun),
    MIN(-6, "MIN", CursorData::min),
    MAX(-7, "MAX", CursorData::max),
    EMPTYRECORD(-8, "EMPTYRECORD", CursorData::emptyRecord),
    RECORD(-9, "RECORD", CursorData::record);

    companion object {
        fun byIndex(index: Int): CursorFun? = values().firstOrNull { it.index == index }

        fun byColumn(funName: String): CursorFun? = values().firstOrNull { it.funName == funName.toUpperCase() }
    }
}

data class Record(var columns: MutableList<Var> = ArrayList() ) {

    fun setApply(sourceRecord: Record) {
        checkExistsColumns(sourceRecord)

        val columnsRecord = ArrayList<Var>()
        for(sourceColumn in sourceRecord.columns) {

            val columnRecord = columnByName(sourceColumn.name)?.apply { result.setVar( sourceColumn.result ) }
                ?: Var(sourceColumn.name, VarResult(sourceColumn.result.type, sourceColumn.result.value))

            columnsRecord += columnRecord
        }

        this.columns = columnsRecord
    }

    private fun checkExistsColumns(sourceRecord: Record) {
        for(column in columns) {
            sourceRecord.columns.firstOrNull { it.name == column.name }
                ?: throw Exception("Record column <${column.name}> absent assigned record  $sourceRecord")
        }
    }

    fun clearData() {
        for(column in columns) {
            column.result.type = VarType.UNDEFINED
            column.result.value = null
        }
    }

    fun columnByName(columnName: String): Var? = columns.firstOrNull { it.name == columnName }
}

data class QuerySession(val query: Query, val sessionSetting: SessionSetting)

private class ColumnResult(private val cursor: CursorData, val columnName: String,
                           private var index: Int = UNINITIALIZE_COLUMN_INDEX,
                           private var funIndex: Int = UNINITIALIZE_COLUMN_INDEX): ReturnResult {
    override fun getVar(): VarResult {
        checkInitIndexes()

        return cursor.getVarResult(index, funIndex)
    }

    override fun setVar(newVar: VarResult) {}

    override fun getSqlValue(): Any {
        checkInitIndexes()

        return cursor.toSqlValue(index, funIndex)
    }

    private fun checkInitIndexes() {
        if(index == UNINITIALIZE_COLUMN_INDEX) {
            val (newIndex, newFun) =  cursor.getColumnIndex(columnName)
            index = newIndex
            funIndex = newFun
        }
    }
}

private const val UNINITIALIZE_COLUMN_INDEX = Int.MAX_VALUE