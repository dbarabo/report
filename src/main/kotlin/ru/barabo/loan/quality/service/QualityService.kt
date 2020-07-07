package ru.barabo.loan.quality.service

import org.slf4j.LoggerFactory
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
import ru.barabo.loan.quality.entity.Quality
import java.sql.Timestamp
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.jvm.javaType

private val logger = LoggerFactory.getLogger(QualityService::class.java)

object QualityService : StoreFilterService<Quality>(AfinaOrm, Quality::class.java),
    ParamsSelect, CrossData<Quality>, StoreListener<List<ClientBook>> {

    override fun selectParams(): Array<Any?>? =
        arrayOf(ClientBookService?.selectedEntity()?.idClient ?: Long::class.javaObjectType, yearDate)

    override fun getRowCount(): Int = dataListCount()

    override fun getRowType(rowIndex: Int): RowType = dataList[rowIndex].rowType

    override fun setValue(value: Any?, rowIndex: Int, propColumn: KMutableProperty1<Quality, Any?>) {

        val row = getEntity(rowIndex) ?: throw Exception("строка №$rowIndex не найдена")

        propColumn.set(row, value)

        val columnName = propColumn.findAnnotation<ColumnName>()?.name?.toUpperCase() ?: throw Exception("ColumnName for property $propColumn not found")

        val sqlDate = dateByColumnName(yearDate, columnName)

        val javaType = propColumn.returnType.javaType as Class<*>

        if(javaType == String::class.javaObjectType) {
            saveRemark(row, value as? String, sqlDate)
        } else {
            saveBall(row, value as? Int, sqlDate)

            val propInt = propColumn as? KMutableProperty1<Quality, Int?> ?: return

            reCalcSum(propInt)

            logger.error("REFRESH_ALL")

            sentRefreshAllListener(EditType.ALL)
        }
    }

    override fun initData() {
        super.initData()

        reCalcSumAll()
    }

    override fun refreshAll(elemRoot: List<ClientBook>, refreshType: EditType) {
        initData()
    }

    init {
        ClientBookService.addListener(this)
    }

    private fun saveBall(entity: Quality, value: Int?, onMonth: Timestamp) {

        val clientId = ClientBookService.selectedEntity()?.idClient ?: throw Exception("Не задан клиент")

        if(clientId == 0L) throw Exception("Не задан клиент")

        val qualityId = entity.id ?: throw Exception("Не задан id строки Quality")

        AfinaQuery.execute(EXEC_SAVE_BALL, arrayOf(value?:Int::class.javaPrimitiveType, clientId, onMonth, qualityId))
    }

    private fun saveRemark(entity: Quality, value: String?, onMonth: Timestamp) {

        val clientId = ClientBookService.selectedEntity()?.idClient ?: throw Exception("Не задан клиент")

        if(clientId == 0L) throw Exception("Не задан клиент")

        val qualityId = entity.id ?: throw Exception("Не задан id строки Quality")

        AfinaQuery.execute(EXEC_SAVE_REMARK, arrayOf(value?:"", clientId, onMonth, qualityId))
    }

    private const val EXEC_SAVE_REMARK = "{ call od.PTKB_LOAN_METHOD_JUR.setRemarkQualityData(?, ?, ?, ?) }"

    private const val EXEC_SAVE_BALL = "{ call od.PTKB_LOAN_METHOD_JUR.setBallQualityData(?, ?, ?, ?) }"


    private fun reCalcSumAll() {
        reCalcSum(Quality::valueMonth1)

        reCalcSum(Quality::valueMonth4)

        reCalcSum(Quality::valueMonth7)

        reCalcSum(Quality::valueMonth10)
    }

    private fun reCalcSum(propColumn: KMutableProperty1<Quality, Int?>) {

        val sumEntity =  getEntity(dataList.size - 1) ?: return

        val sum = dataList
            .take(dataList.size - 1)
            .map { propColumn.get(it) }
            .sumBy { it?:0 }

        propColumn.set(sumEntity, sum)
    }
}

private fun dateByColumnName(yearDate: Timestamp, columnName: String): Timestamp {

    val addMonth: Long = when(columnName[columnName.lastIndex]) {
        '1' ->   0L
        '4' ->   3L
        '7' ->   6L
        '0' ->  9L
        else -> throw Exception("columnName value unsupported $columnName")
    }

    return Timestamp.valueOf( yearDate.toLocalDateTime().toLocalDate().plusMonths(addMonth).atStartOfDay())
}