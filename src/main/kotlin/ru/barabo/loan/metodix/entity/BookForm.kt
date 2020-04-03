package ru.barabo.loan.metodix.entity

import ru.barabo.db.annotation.ColumnName
import ru.barabo.db.annotation.SelectQuery
import ru.barabo.gui.swing.cross.RowType

@SelectQuery("{ ? = call OD.PTKB_LOAN_METHOD_JUR.getBookForm(?, ?, ?) }")
data class BookForm (

    @ColumnName("ID")
    var id: Long? = null,

    @ColumnName("CODE")
    var code: String? = null,

    @ColumnName("NAME")
    var name: String? = null,

    @ColumnName("TYPE_")
    var type: Int = 0,

    @ColumnName("FORMULA")
    var formula: String? = null,

    @ColumnName("VALUE_M1")
    var valueMonth1: Int? = null,

    @ColumnName("VALUE_M4")
    var valueMonth4: Int? = null,

    @ColumnName("VALUE_M7")
    var valueMonth7: Int? = null,

    @ColumnName("VALUE_M10")
    var valueMonth10: Int? = null
) {
    val rowType: RowType
        get() = RowType.rowTypeByDbValue(type)
}