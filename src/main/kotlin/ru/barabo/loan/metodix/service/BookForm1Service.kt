package ru.barabo.loan.metodix.service

import ru.barabo.afina.AfinaOrm
import ru.barabo.afina.AfinaQuery
import ru.barabo.db.EditType
import ru.barabo.db.annotation.ParamsSelect
import ru.barabo.db.service.StoreFilterService
import ru.barabo.db.service.StoreListener
import ru.barabo.gui.swing.cross.CrossData
import ru.barabo.gui.swing.cross.RowType
import ru.barabo.loan.metodix.entity.BookForm
import ru.barabo.loan.metodix.entity.ClientBook
import java.sql.Timestamp
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

object BookForm1Service : StoreFilterService<BookForm>(AfinaOrm, BookForm::class.java),
    ParamsSelect, CrossData<BookForm>, StoreListener<List<ClientBook>> {

    var yearDate: Timestamp = Timestamp(Date().time)
        private set

    override fun selectParams(): Array<Any?>? = arrayOf(ClientBookService?.selectedEntity()?.idClient ?: Long::class.javaObjectType, 0, yearDate)

    override fun refreshAll(elemRoot: List<ClientBook>, refreshType: EditType) {
        initData()
    }

    override fun getEntity(rowIndex: Int): BookForm? = if(dataList.size > rowIndex) dataList[rowIndex] else null

    override fun getRowType(rowIndex: Int): RowType = dataList[rowIndex].rowType

    override fun getRowCount(): Int = dataListCount()

    override fun setCellValue(value: Any?, columnIndex: Int, rowIndex: Int) {
        TODO("Not yet implemented")
    }

    var year: String
        get() = DateTimeFormatter.ofPattern("yyyy").format( yearDate.toLocalDateTime() )
        set(value) {
            yearDate = Timestamp.valueOf(LocalDate.parse("${value.trim()}-01-01").atStartOfDay())

            initData()
        }

    init {

        ClientBookService.addListener(this)
    }
}

