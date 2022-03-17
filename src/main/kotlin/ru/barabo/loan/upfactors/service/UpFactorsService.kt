package ru.barabo.loan.upfactors.service

import ru.barabo.afina.AfinaOrm
import ru.barabo.afina.AfinaQuery
import ru.barabo.db.EditType
import ru.barabo.db.annotation.ColumnName
import ru.barabo.db.annotation.ParamsSelect
import ru.barabo.db.service.StoreFilterService
import ru.barabo.db.service.StoreListener
import ru.barabo.gui.swing.cross.CrossData
import ru.barabo.gui.swing.cross.RowType
import ru.barabo.loan.upfactors.entity.UpFactors
import ru.barabo.loan.metodix.entity.ClientBook
import ru.barabo.loan.metodix.service.ClientBookService
import ru.barabo.loan.metodix.service.yearDate
import ru.barabo.loan.quality.service.ParamsClientYear
import ru.barabo.loan.quality.service.dateByColumnName
import java.sql.Timestamp
import java.util.*
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.jvm.javaType

object UpFactorsService   : StoreFilterService<UpFactors>(AfinaOrm, UpFactors::class.java),
    ParamsSelect, CrossData<UpFactors>, StoreListener<List<ClientBook>> {

    override fun selectParams(): Array<Any?> = ParamsClientYear.selectParams()

    override fun getRowCount(): Int = dataListCount()

    override fun getRowType(rowIndex: Int): RowType = dataList[rowIndex].rowType

    override fun refreshAll(elemRoot: List<ClientBook>, refreshType: EditType) {
        initData()
    }

    init {
        ClientBookService.addListener(this)
    }

    fun pasteFromTemplate(propColumn: KMutableProperty1<UpFactors, Any?>) {
        val clientId = ClientBookService.selectedEntity()?.idClient?.takeIf { it != 0L } ?: throw Exception("Не задан клиент")

        val columnName = propColumn.findAnnotation<ColumnName>()?.name?.uppercase(Locale.getDefault()) ?: throw Exception("ColumnName for property $propColumn not found")

        val sqlDate = dateByColumnName(yearDate, columnName)

        AfinaQuery.execute(EXEC_FROM_TEMPLATE, arrayOf(clientId, sqlDate))

        initData()
    }

    override fun setValue(value: Any?, rowIndex: Int, propColumn: KMutableProperty1<UpFactors, Any?>) {
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

    private fun saveTemplate(entity: UpFactors, value: String?) {

        val id = entity.id ?: throw Exception("Не задан id строки UpFactors")

        AfinaQuery.execute(EXEC_SAVE_TEMPLATE, arrayOf(value?:"", id))
    }

    private fun saveRemark(entity: UpFactors, value: String?, onMonth: Timestamp) {

        val clientId = ClientBookService.selectedEntity()?.idClient?.takeIf { it != 0L } ?: throw Exception("Не задан клиент")

        val id = entity.id ?: throw Exception("Не задан id строки UpFactors")

        AfinaQuery.execute(EXEC_SAVE_REMARK, arrayOf(value?:"", clientId, onMonth, id))
    }

    private fun saveCheck(entity: UpFactors, value: Int?, onMonth: Timestamp) {
        val clientId = ClientBookService.selectedEntity()?.idClient
            ?.takeIf { it != 0L } ?: throw Exception("Не задан клиент")

        val id = entity.id ?: throw Exception("Не задан id строки UpFactors")

        AfinaQuery.execute(EXEC_SAVE_CHECK, arrayOf(value?:Int::class.javaPrimitiveType, clientId, onMonth, id))
    }

    private const val EXEC_SAVE_TEMPLATE = "update od.PTKB_LOAN_UP_FACTORS set TEMPLATE_REMARK = ? where ID = ?"

    private const val EXEC_SAVE_REMARK = "{ call od.PTKB_LOAN_METHOD_JUR.setRemarkUpFactors(?, ?, ?, ?) }"

    private const val EXEC_SAVE_CHECK = "{ call od.PTKB_LOAN_METHOD_JUR.setCheckUpFactors(?, ?, ?, ?) }"

    private const val EXEC_FROM_TEMPLATE = "{ call od.PTKB_LOAN_METHOD_JUR.pasteUpFactorsFromTemplate(?, ?) }"
}