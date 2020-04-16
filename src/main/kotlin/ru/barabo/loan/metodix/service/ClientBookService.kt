package ru.barabo.loan.metodix.service

import ru.barabo.afina.AfinaOrm
import ru.barabo.afina.AfinaQuery
import ru.barabo.db.service.StoreFilterService
import ru.barabo.loan.metodix.entity.ClientBook
import ru.barabo.report.service.ReportService
import ru.barabo.selector.entity.ClientWithAccount
import ru.barabo.xls.Var
import ru.barabo.xls.VarResult
import ru.barabo.xls.VarType
import java.lang.Exception

object ClientBookService : StoreFilterService<ClientBook>(AfinaOrm, ClientBook::class.java) {

    val yearBooks: List<String>
        get() {
            if(::years.isInitialized) return years

            years = initYears()

            return years
        }

    private lateinit var years: List<String>

    private fun initYears(): List<String> = AfinaQuery.selectCursor(SELECT_YEARS).map { it[0] as String }

    private const val SELECT_YEARS = "{ ? = call od.PTKB_LOAN_METHOD_JUR.getYearsBookForm }"

    fun addNewClient(newClient: ClientWithAccount): ClientBook {
        val client = dataList.firstOrNull { it.idClient == newClient.id } ?:
            ClientBook(newClient.sortLabel, newClient.label, newClient.id).apply {  callBackSelectData(this) }

        setSelectedEntity(client)

        return client
    }

    fun runReportBookForm(formNumber: Int) {
        if(formNumber !in listOf(0, 1)) throw Exception("Экспорт в Excel возможен только для бух. форм №1 и №2")

        val vars = arrayListOf(
            Var("FORM_NUMBER", VarResult(VarType.INT, formNumber) ),
            Var("ID_CLIENT", VarResult(VarType.INT, selectedEntity()?.idClient) ),
            Var("YEAR_DATE", VarResult(VarType.DATE, yearDate) )
        )

        val idBookReport = 1210481813L
        ReportService.runDinamicReport(idBookReport, vars)
    }
}