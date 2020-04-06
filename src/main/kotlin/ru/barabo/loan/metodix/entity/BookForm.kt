package ru.barabo.loan.metodix.entity

import ru.barabo.db.annotation.ColumnName
import ru.barabo.db.annotation.SelectQuery
import ru.barabo.gui.swing.cross.RowType
import kotlin.reflect.KMutableProperty1

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

    @ColumnName("VALUE_MINUS9")
    var valueMonthMinus9: Int? = null,

    @ColumnName("VALUE_MINUS6")
    var valueMonthMinus6: Int? = null,

    @ColumnName("VALUE_MINUS3")
    var valueMonthMinus3: Int? = null,

    @ColumnName("VALUE_M1")
    var valueMonth1: Int? = null,

    @ColumnName("VALUE_M4")
    var valueMonth4: Int? = null,

    @ColumnName("VALUE_M7")
    var valueMonth7: Int? = null,

    @ColumnName("VALUE_M10")
    var valueMonth10: Int? = null,

    @ColumnName("VALUE_PLUS12")
    var valueMonthPlus12: Int? = null
) {
    val rowType: RowType
        get() = RowType.rowTypeByDbValue(type)
}

val BookFormValueList: List<KMutableProperty1<BookForm, Int?>> = listOf(
    BookForm::valueMonthMinus9,
    BookForm::valueMonthMinus6,
    BookForm::valueMonthMinus3,
    BookForm::valueMonth1,
    BookForm::valueMonth4,
    BookForm::valueMonth7,
    BookForm::valueMonth10,
    BookForm::valueMonthPlus12
)

