package ru.barabo.selector.entity

import ru.barabo.db.annotation.ColumnName
import ru.barabo.db.annotation.Filtered
import java.sql.Timestamp
import java.text.SimpleDateFormat

private val dateFormat = SimpleDateFormat("dd.MM.yyyy")

data class ClientWithAccount(
    @ColumnName("CLASSIFIED")
    var id: Long? = null,

    @ColumnName("LABEL")
    @Filtered(0, 0) // stub for mode
    var label: String? = null,

    @ColumnName("TAXID")
    var tax: String? = null,

    @ColumnName("CODE")
    var accountCode: String? = null,

    @ColumnName("OPENED")
    var open: Timestamp? = null,

    @ColumnName("SORT_LABEL")
    var sortLabel: String? = null
) {
    var openFormat: String
        get()  = open?.let { dateFormat.format(it) } ?: ""
        set(_) {}
}

