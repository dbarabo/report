package ru.barabo.loan.ratingactivity.service

import ru.barabo.afina.AfinaOrm
import ru.barabo.afina.AfinaQuery
import ru.barabo.db.EditType
import ru.barabo.db.annotation.ColumnName
import ru.barabo.db.annotation.ParamsSelect
import ru.barabo.db.service.StoreFilterService
import ru.barabo.db.service.StoreListener
import ru.barabo.gui.swing.cross.CrossData
import ru.barabo.gui.swing.cross.RowType
import ru.barabo.loan.metodix.entity.ClientBook
import ru.barabo.loan.metodix.service.ClientBookService
import ru.barabo.loan.metodix.service.yearDate
import ru.barabo.loan.quality.service.ParamsClientYear
import ru.barabo.loan.quality.service.dateByColumnName
import ru.barabo.loan.ratingactivity.entity.RatingActivity
import java.sql.Timestamp
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.jvm.javaType

object RatingActivityService : StoreFilterService<RatingActivity>(AfinaOrm, RatingActivity::class.java),
    ParamsSelect, CrossData<RatingActivity>, StoreListener<List<ClientBook>> {

    override fun selectParams(): Array<Any?>? = ParamsClientYear.selectParams()

    override fun getRowCount(): Int = dataListCount()

    override fun getRowType(rowIndex: Int): RowType = dataList[rowIndex].rowType

    override fun refreshAll(elemRoot: List<ClientBook>, refreshType: EditType) {
        initData()
    }

    init {
        ClientBookService.addListener(this)
    }

    fun pasteFromTemplate(propColumn: KMutableProperty1<RatingActivity, Any?>) {
        val clientId = ClientBookService.selectedEntity()?.idClient?.takeIf { it != 0L } ?: throw Exception("Не задан клиент")

        val columnName = propColumn.findAnnotation<ColumnName>()?.name?.toUpperCase() ?: throw Exception("ColumnName for property $propColumn not found")

        val sqlDate = dateByColumnName(yearDate, columnName)

        AfinaQuery.execute(EXEC_FROM_TEMPLATE, arrayOf(clientId, sqlDate))

        initData()
    }

    override fun setValue(value: Any?, rowIndex: Int, propColumn: KMutableProperty1<RatingActivity, Any?>) {
        val entity = getEntity(rowIndex) ?: throw Exception("строка №$rowIndex не найдена")

        propColumn.set(entity, value)

        val columnName = propColumn.findAnnotation<ColumnName>()?.name?.toUpperCase() ?: throw Exception("ColumnName for property $propColumn not found")

        val sqlDate = try {
            dateByColumnName(yearDate, columnName)
        } catch (e: java.lang.Exception) {
            null
        }

        val javaType = propColumn.returnType.javaType as Class<*>

        when {
            (sqlDate == null) && (javaType == String::class.javaObjectType) -> saveTemplate(entity, value as? String)
            (sqlDate != null) && (javaType == String::class.javaObjectType) -> saveRemark(entity, value as? String, sqlDate)
            else -> saveCheck(entity, value as? Int, sqlDate!!)
        }
    }

    private fun saveTemplate(entity: RatingActivity, value: String?) {

        val id = entity.id ?: throw Exception("Не задан id строки RatingActivity")

        AfinaQuery.execute(EXEC_SAVE_TEMPLATE, arrayOf(value?:"", id))
    }

    private fun saveRemark(entity: RatingActivity, value: String?, onMonth: Timestamp) {

        val clientId = ClientBookService.selectedEntity()?.idClient?.takeIf { it != 0L } ?: throw Exception("Не задан клиент")

        val id = entity.id ?: throw Exception("Не задан id строки RatingActivity")

        AfinaQuery.execute(EXEC_SAVE_REMARK, arrayOf(value?:"", clientId, onMonth, id))
    }

    private fun saveCheck(entity: RatingActivity, value: Int?, onMonth: Timestamp) {
        val clientId = ClientBookService.selectedEntity()?.idClient
                ?.takeIf { it != 0L } ?: throw Exception("Не задан клиент")

        val id = entity.id ?: throw Exception("Не задан id строки RatingActivity")

        AfinaQuery.execute(EXEC_SAVE_CHECK, arrayOf(value?:Int::class.javaPrimitiveType, clientId, onMonth, id))
    }

    private const val EXEC_SAVE_TEMPLATE = "update od.PTKB_LOAN_RATING_ACTIVITY set TEMPLATE_REMARK = ? where ID = ?"

    private const val EXEC_SAVE_REMARK = "{ call od.PTKB_LOAN_METHOD_JUR.setRemarkRatingActivity(?, ?, ?, ?) }"

    private const val EXEC_SAVE_CHECK = "{ call od.PTKB_LOAN_METHOD_JUR.setCheckRatingActivity(?, ?, ?, ?) }"

    private const val EXEC_FROM_TEMPLATE = "{ call od.PTKB_LOAN_METHOD_JUR.pasteRatingRemarkFromTemplate(?, ?) }"
}
