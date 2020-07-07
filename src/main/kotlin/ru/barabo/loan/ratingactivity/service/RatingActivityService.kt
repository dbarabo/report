package ru.barabo.loan.ratingactivity.service

import ru.barabo.afina.AfinaOrm
import ru.barabo.db.EditType
import ru.barabo.db.annotation.ParamsSelect
import ru.barabo.db.service.StoreFilterService
import ru.barabo.db.service.StoreListener
import ru.barabo.gui.swing.cross.CrossData
import ru.barabo.gui.swing.cross.RowType
import ru.barabo.loan.metodix.entity.ClientBook
import ru.barabo.loan.metodix.service.ClientBookService
import ru.barabo.loan.quality.service.ParamsClientYear
import ru.barabo.loan.ratingactivity.entity.RatingActivity
import kotlin.reflect.KMutableProperty1

object RatingActivityService : StoreFilterService<RatingActivity>(AfinaOrm, RatingActivity::class.java),
    ParamsSelect, CrossData<RatingActivity>, StoreListener<List<ClientBook>> {

    override fun selectParams(): Array<Any?>? = ParamsClientYear.selectParams()

    override fun getRowCount(): Int = dataListCount()

    override fun getRowType(rowIndex: Int): RowType = dataList[rowIndex].rowType

    override fun setValue(value: Any?, rowIndex: Int, propColumn: KMutableProperty1<RatingActivity, Any?>) {
        TODO("Not yet implemented")
    }

    override fun refreshAll(elemRoot: List<ClientBook>, refreshType: EditType) {
        initData()
    }

    init {
        ClientBookService.addListener(this)
    }
}