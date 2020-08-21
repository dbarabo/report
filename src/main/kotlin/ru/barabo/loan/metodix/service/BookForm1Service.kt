@file:Suppress("UNCHECKED_CAST")

package ru.barabo.loan.metodix.service

import ru.barabo.afina.AfinaOrm
import ru.barabo.afina.AfinaQuery
import ru.barabo.db.EditType
import ru.barabo.db.annotation.ColumnName
import ru.barabo.db.annotation.ParamsSelect
import ru.barabo.db.service.StoreFilterService
import ru.barabo.db.service.StoreListener
import ru.barabo.gui.swing.cross.CrossData
import ru.barabo.gui.swing.cross.FormulaCalc
import ru.barabo.gui.swing.cross.RowType
import ru.barabo.loan.metodix.entity.BookForm
import ru.barabo.loan.metodix.entity.BookFormValueList
import ru.barabo.loan.metodix.entity.ClientBook
import ru.barabo.loan.quality.service.QualityService
import java.sql.Timestamp
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.full.findAnnotation

var yearDate: Timestamp = Timestamp.valueOf(LocalDate.now().withDayOfYear(1).atStartOfDay() )
    private set

var year: String
    get() = DateTimeFormatter.ofPattern("yyyy").format( yearDate.toLocalDateTime() )
    set(value) {
        yearDate = Timestamp.valueOf(LocalDate.parse("${value.trim()}-01-01").atStartOfDay())

        BookForm1Service.initData()
        BookForm2Service.initData()
        QualityService.initData()
    }

open class BookFormService(private val forma: Int) : StoreFilterService<BookForm>(AfinaOrm, BookForm::class.java),
    ParamsSelect, CrossData<BookForm>, StoreListener<List<ClientBook>> {

    private val formulaCalc =
        FormulaCalc(this, BookForm::formula, BookForm::code, BookFormValueList)

    override fun selectParams(): Array<Any?>? = arrayOf(ClientBookService?.selectedEntity()?.idClient ?: Long::class.javaObjectType, forma, yearDate)

    override fun refreshAll(elemRoot: List<ClientBook>, refreshType: EditType) {
        initData()
    }

    override fun initData() {
        super.initData()

        formulaCalc?.calc()
    }

    override fun getEntity(rowIndex: Int): BookForm? = if(dataList.size > rowIndex) dataList[rowIndex] else null

    override fun getRowType(rowIndex: Int): RowType = dataList[rowIndex].rowType

    override fun getRowCount(): Int = dataListCount()

    override fun setValue(value: Any?, rowIndex: Int, propColumn: KMutableProperty1<BookForm, Any?>) {
        val row = getEntity(rowIndex) ?: throw Exception("строка №$rowIndex не найдена")

        val intValue = toIntValue(value)

        propColumn.set(row, intValue)

        val columnName = propColumn.findAnnotation<ColumnName>()?.name?.toUpperCase() ?: throw Exception("ColumnName for property $propColumn not found")

        val sqlDate = dateByColumnName(yearDate, columnName)

        saveDb(row, intValue, sqlDate, forma)
        formulaCalc.calc(propColumn as KMutableProperty1<BookForm, Int?>)

        sentRefreshAllListener(EditType.EDIT)
    }

    init {
        ClientBookService.addListener(this)
    }
}

object BookForm1Service : BookFormService(0)

object BookForm2Service : BookFormService(1)

private const val EXEC_SAVE_VALUE = "{ call od.PTKB_LOAN_METHOD_JUR.setValueFormClientBalance(?, ?, ?, ?, ?) }"

fun saveDb(row: BookForm, value: Int?, period: Timestamp, forma: Int) {

    val clientId = ClientBookService.selectedEntity()?.idClient ?: throw Exception("Не задан клиент")

    if(clientId == 0L) throw Exception("Не задан клиент")

    val code = row.code ?: throw Exception("Не задан код формы")

    AfinaQuery.execute(EXEC_SAVE_VALUE, arrayOf(value?:Long::class.javaPrimitiveType, clientId, period, code, forma))
}

fun toIntValue(value: Any?): Int? {
    return when(value) {
        null -> null
        is Number -> value.toInt()
        is String -> value.trim().toIntOrNull()
        else -> throw Exception("type for $value incorrect")
    }
}

private fun dateByColumnName(yearDate: Timestamp, columnName: String): Timestamp {

    val addMonth: Long = when(columnName) {
        "VALUE_MINUS9" ->   -9L
        "VALUE_MINUS6" ->   -6L
        "VALUE_MINUS3" ->   -3L
        "VALUE_M1" ->   0L
        "VALUE_M4" ->   3L
        "VALUE_M7" ->   6L
        "VALUE_M10" ->  9L
        "VALUE_PLUS12" ->   12L
        else -> throw Exception("columnName value unsupported $columnName")
    }

    return Timestamp.valueOf( yearDate.toLocalDateTime().toLocalDate().plusMonths(addMonth).atStartOfDay())
}