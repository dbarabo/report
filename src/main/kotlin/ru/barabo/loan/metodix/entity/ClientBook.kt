package ru.barabo.loan.metodix.entity

import ru.barabo.db.annotation.ColumnName
import ru.barabo.db.annotation.SelectQuery

@SelectQuery("{ ? = call OD.PTKB_LOAN_METHOD_JUR.getClientBookForm }")
data class ClientBook(
    @ColumnName("SORT_LABEL")
    var sortLabel: String? = null,

    @ColumnName("LABEL")
    var label: String? = null,

    @ColumnName("ID_CLIENT")
    var idClient: Long? = null
) {
    override fun toString(): String = sortLabel ?: ""
}