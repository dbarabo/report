package ru.barabo.loan.msfo.gui

import ru.barabo.gui.swing.AbstractDialog
import ru.barabo.gui.swing.comboBox
import ru.barabo.loan.metodix.gui.formatDateAdd
import ru.barabo.loan.metodix.gui.yearFormatter
import ru.barabo.loan.metodix.service.yearDate
import ru.barabo.loan.msfo.service.XlsxBuilder
import java.awt.Component
import java.time.LocalDate
import java.time.temporal.ChronoField
import javax.swing.JComboBox

class DialogSelectMonth(component: Component) : AbstractDialog(component, "Выбор отчетной даты") {

    private val comboMonth: JComboBox<String>

    private val comboLoans: JComboBox<String>

    init {

        comboBox("Отчетная дата", 0, listQuartals() ).apply {
            comboMonth = this
        }

        val loans = XlsxBuilder.getCreditInfoListByClient()

        comboBox("Кредитный договор", 1, loans ).apply {
            comboLoans = this
        }

        createOkCancelButton(2, 1)

        packWithLocation()
    }

    override fun okProcess() {

        val selectDate = yearFormatter.parse(comboMonth.selectedItem as? String)

        val reportDate = LocalDate.of(selectDate.get(ChronoField.YEAR), selectDate.get(ChronoField.MONTH_OF_YEAR), 1)

        val loan = comboLoans.selectedItem as String

        XlsxBuilder.processCopy(LocalDate.from(reportDate), loan)
    }

    private fun listQuartals(): List<String> = listOf(
        formatDateAdd(yearDate, -3),
        formatDateAdd(yearDate),
        formatDateAdd(yearDate, 3),
        formatDateAdd(yearDate, 6),
        formatDateAdd(yearDate, 9),
        formatDateAdd(yearDate, 12)
    )
}