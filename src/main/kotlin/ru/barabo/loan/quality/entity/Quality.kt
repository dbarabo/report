package ru.barabo.loan.quality.entity

import ru.barabo.db.annotation.ColumnName
import ru.barabo.db.annotation.SelectQuery
import ru.barabo.gui.swing.cross.RowType

@SelectQuery("{ ? = call OD.PTKB_LOAN_METHOD_JUR.getQualityData(?, ?) }")
data class Quality(

    @ColumnName("ID")
    var id: Long? = null,

    @ColumnName("TYPE_ROW")
    var typeRow: Int = 0,

    @ColumnName("NAME")
    var name: String? = null,

    @ColumnName("BALL_LIST")
    var ballList: String? = null,

    @ColumnName("VALUE_M1")
    var valueMonth1: Int? = null,

    @ColumnName("REM_M1")
    var remarkMonth1: String? = null,

    @ColumnName("VALUE_M4")
    var valueMonth4: Int? = null,

    @ColumnName("REM_M4")
    var remarkMonth4: String? = null,

    @ColumnName("VALUE_M7")
    var valueMonth7: Int? = null,

    @ColumnName("REM_M7")
    var remarkMonth7: String? = null,

    @ColumnName("VALUE_M10")
    var valueMonth10: Int? = null,

    @ColumnName("REM_M10")
    var remarkMonth10: String? = null
) {
    val rowType: RowType
        get() = RowType.rowTypeByDbValue(typeRow)
}