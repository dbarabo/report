package ru.barabo.loan.quality.service

import ru.barabo.afina.AfinaOrm
import ru.barabo.db.EditType
import ru.barabo.db.annotation.ParamsSelect
import ru.barabo.db.service.StoreFilterService
import ru.barabo.db.service.StoreListener
import ru.barabo.gui.swing.cross.CrossData
import ru.barabo.gui.swing.cross.RowType
import ru.barabo.loan.metodix.entity.ClientBook
import ru.barabo.loan.metodix.service.ClientBookService
import ru.barabo.loan.metodix.service.yearDate
import ru.barabo.loan.quality.entity.Quality
import kotlin.reflect.KMutableProperty1

object QualityService : StoreFilterService<Quality>(AfinaOrm, Quality::class.java),
    ParamsSelect, CrossData<Quality>, StoreListener<List<ClientBook>> {

    override fun selectParams(): Array<Any?>? =
        arrayOf(ClientBookService?.selectedEntity()?.idClient ?: Long::class.javaObjectType, yearDate)

    override fun getRowCount(): Int = dataListCount()

    override fun getRowType(rowIndex: Int): RowType = dataList[rowIndex].rowType

    override fun setValue(value: Any?, rowIndex: Int, propColumn: KMutableProperty1<Quality, Any?>) {

        val row = getEntity(rowIndex) ?: throw Exception("строка №$rowIndex не найдена")

        propColumn.set(row, value)
    }

    override fun refreshAll(elemRoot: List<ClientBook>, refreshType: EditType) {
        initData()
    }

    init {
        ClientBookService.addListener(this)
    }
}