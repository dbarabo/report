package ru.barabo.loan.excludewell.service

import ru.barabo.afina.AfinaOrm
import ru.barabo.afina.AfinaQuery
import ru.barabo.db.EditType
import ru.barabo.db.annotation.ColumnName
import ru.barabo.db.annotation.ParamsSelect
import ru.barabo.db.service.StoreFilterService
import ru.barabo.db.service.StoreListener
import ru.barabo.gui.swing.cross.CrossData
import ru.barabo.gui.swing.cross.RowType
import ru.barabo.loan.excludewell.entity.ExcludeWellState
import ru.barabo.loan.metodix.entity.ClientBook
import ru.barabo.loan.metodix.service.ClientBookService
import ru.barabo.loan.metodix.service.yearDate
import ru.barabo.loan.quality.service.ParamsClientYear
import ru.barabo.loan.quality.service.dateByColumnName
import ru.barabo.loan.threateningtrend.service.ThreateningTrendService
import java.sql.Timestamp
import java.util.*
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.jvm.javaType

object ExcludeWellStateService   : StoreFilterService<ExcludeWellState>(AfinaOrm, ExcludeWellState::class.java),
    ParamsSelect, CrossData<ExcludeWellState>, StoreListener<List<ClientBook>> {

    override fun selectParams(): Array<Any?> = ParamsClientYear.selectParams()

    override fun getRowCount(): Int = ThreateningTrendService.dataListCount()

    override fun getRowType(rowIndex: Int): RowType = ExcludeWellStateService.dataList[rowIndex].rowType

    override fun refreshAll(elemRoot: List<ClientBook>, refreshType: EditType) {
        initData()
    }

    init {
        ClientBookService.addListener(this)
    }

    fun pasteFromTemplate(propColumn: KMutableProperty1<ExcludeWellState, Any?>) {
        val clientId = ClientBookService.selectedEntity()?.idClient?.takeIf { it != 0L } ?: throw Exception("Не задан клиент")

        val columnName = propColumn.findAnnotation<ColumnName>()?.name?.uppercase(Locale.getDefault()) ?: throw Exception("ColumnName for property $propColumn not found")

        val sqlDate = dateByColumnName(yearDate, columnName)

        AfinaQuery.execute(EXEC_FROM_TEMPLATE, arrayOf(clientId, sqlDate))

        initData()
    }

    override fun setValue(value: Any?, rowIndex: Int, propColumn: KMutableProperty1<ExcludeWellState, Any?>) {
        val entity = getEntity(rowIndex) ?: throw Exception("строка №$rowIndex не найдена")

        propColumn.set(entity, value)

        val columnName = propColumn.findAnnotation<ColumnName>()?.name?.uppercase(Locale.getDefault()) ?: throw Exception("ColumnName for property $propColumn not found")

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

    private fun saveTemplate(entity: ExcludeWellState, value: String?) {

        val id = entity.id ?: throw Exception("Не задан id строки ExcludeWellState")

        AfinaQuery.execute(EXEC_SAVE_TEMPLATE, arrayOf(value?:"", id))
    }

    private fun saveRemark(entity: ExcludeWellState, value: String?, onMonth: Timestamp) {

        val clientId = ClientBookService.selectedEntity()?.idClient?.takeIf { it != 0L } ?: throw Exception("Не задан клиент")

        val id = entity.id ?: throw Exception("Не задан id строки ExcludeWellState")

        AfinaQuery.execute(EXEC_SAVE_REMARK, arrayOf(value?:"", clientId, onMonth, id))
    }

    private fun saveCheck(entity: ExcludeWellState, value: Int?, onMonth: Timestamp) {
        val clientId = ClientBookService.selectedEntity()?.idClient
            ?.takeIf { it != 0L } ?: throw Exception("Не задан клиент")

        val id = entity.id ?: throw Exception("Не задан id строки ExcludeWellState")

        AfinaQuery.execute(EXEC_SAVE_CHECK, arrayOf(value?:Int::class.javaPrimitiveType, clientId, onMonth, id))
    }

    private const val EXEC_SAVE_TEMPLATE = "update od.EXCLUDE_WELL_STATE set TEMPLATE_REMARK = ? where ID = ?"

    private const val EXEC_SAVE_REMARK = "{ call od.PTKB_LOAN_METHOD_JUR.setRemarkExcludeWellState(?, ?, ?, ?) }"

    private const val EXEC_SAVE_CHECK = "{ call od.PTKB_LOAN_METHOD_JUR.setCheckExcludeWellState(?, ?, ?, ?) }"

    private const val EXEC_FROM_TEMPLATE = "{ call od.PTKB_LOAN_METHOD_JUR.pasteExcludeWellFromTemplate(?, ?) }"
}