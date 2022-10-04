package ru.barabo.loan.msfo.service

import org.apache.poi.ss.usermodel.CellType
import org.apache.poi.ss.usermodel.FormulaEvaluator
import org.apache.poi.ss.usermodel.WorkbookFactory
import org.apache.poi.xssf.usermodel.XSSFSheet
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import ru.barabo.afina.AfinaQuery
import ru.barabo.loan.metodix.service.ClientBookService
import ru.barabo.report.entity.defaultReportDirectory
import java.awt.Desktop
import java.io.File
import java.io.FileOutputStream
import java.sql.Date
import java.sql.Timestamp
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.IsoFields

object XlsxBuilder {

    fun processCopy(reportDate: LocalDate, loanInfo: String, lgdRate: Number) {

        val clientLabel = ClientBookService.selectedEntity()?.label ?: throw Exception("Клиент не выбран")

        val savePathFile = fullSaveFilePath(clientLabel.trimAll(), reportDate)

        val pathTemplate = XlsxBuilder::class.java.getResource(MSFO_TEMPLATE)?.openStream() ?: throw Exception("not open $MSFO_TEMPLATE")

        pathTemplate.use { stream ->
            val workbook: XSSFWorkbook = WorkbookFactory.create(stream) as XSSFWorkbook

            FileOutputStream(savePathFile).use {
                workbook.use { book ->

                    val evaluator: FormulaEvaluator = book.creationHelper.createFormulaEvaluator()

                    evaluator.clearAllCachedResultValues()

                    book.forceFormulaRecalculation = true

                    book.processBook(reportDate, loanInfo, lgdRate)

                    book.write(FileOutputStream(savePathFile))
                }
            }
        }
        Desktop.getDesktop().open(savePathFile)
    }

    fun getCreditInfoListByClient(): List<String> {
        val clientId = ClientBookService.selectedEntity()?.idClient ?: throw Exception("Клиент не выбран")

        val creditInfo = AfinaQuery.selectCursor(SELECT_CREDIT_INFO, arrayOf(clientId)).takeIf { it.isNotEmpty() }
            ?: throw Exception("По клиенту не найдены кредиты")

        return creditInfo.map { "№${it[0]} от ${it[1]}г." }
    }

    fun getLgdTypes(): List<String> {
        val info = AfinaQuery.selectCursor(SELECT_LGD_TYPES)

        return info.map { "${it[0]}\t ${it[1]}%" }
    }

    fun getLgdRate(lgdType: String, reportDate: LocalDate): Number {

        val type = lgdType.substringBefore('\t').trim()

        return AfinaQuery.selectValue(SELECT_LGD_RATE, arrayOf(type,
            Date(Timestamp.valueOf(reportDate.atStartOfDay()).time))) as Number
    }

    private fun fullSaveFilePath(clientName: String, reportDate: LocalDate,
                                 now: LocalDateTime = LocalDateTime.now()): File {

        val savePathFile =
            File("${defaultReportDirectory().absolutePath}/${fileName(clientName, reportDate, now)}")

        return if(savePathFile.exists())
            fullSaveFilePath(clientName, reportDate, now.plusSeconds(1))
                else savePathFile
    }

    private fun fileName(clientName: String, reportDate: LocalDate, now: LocalDateTime) =
        "msfo-${clientName}-${reportDate.formatDateYyyyMmDd()}-${now.formatDateTime()}.xlsx"

    private const val MSFO_TEMPLATE = "/xlsx/msfo.xlsx"
}

private fun String.trimAll() = this.replace("[^\\\\da-zA-Zа-яА-Я]".toRegex(), "") //  replace("\"", "").replace(" ", "")

private fun LocalDate.formatDateYyyyMmDd() = DateTimeFormatter.ofPattern("yyyy-MM-dd").format(this)

private fun LocalDate.formatDateInXlsx() = DateTimeFormatter.ofPattern("dd.MM.yyyy").format(this)


private fun XSSFWorkbook.processBook(reportDate: LocalDate, loanInfo: String, lgdRate: Number) {

    val clientId = ClientBookService.selectedEntity()?.idClient ?: throw Exception("Клиент не выбран")

    val params = arrayOf<Any?>(clientId, Date(Timestamp.valueOf(reportDate.atStartOfDay()).time))

    val data = AfinaQuery.selectCursor(SELECT_DATA_FORM1, params)

    getSheetAt(0).setClientName(loanInfo, lgdRate)

    val sheet = this.getSheet("Рейтинг")

    sheet.setReportDate(reportDate)

    sheet.setDataForm(data)
}

private fun XSSFSheet.setClientName(loanInfo: String, lgdRate: Number) {

    val clientId = ClientBookService.selectedEntity()?.idClient ?: throw Exception("Клиент не выбран")

    val clientName  = ClientBookService.selectedEntity()?.label ?: throw Exception("Клиент не выбран")

    val creditInfo = AfinaQuery.selectCursor(SELECT_CREDIT_INFO_LOAN, arrayOf(clientId, loanInfo)).takeIf { it.isNotEmpty() }?.get(0)
        ?: throw Exception("По клиенту не найдены кредиты")

    val creditHeader = "Кредитный договор №${creditInfo[0]} от ${creditInfo[1]}г."

    val rating = creditInfo[5]?.toString() ?: ""

    getRow(ROW_CLIENT_NAME).getCell(COL_CLIENT_NAME).setCellValue(clientName)

    with ( getRow(ROW_CREDIT_INFO) ) {
        getCell(COL_CLIENT_NAME).setCellValue(creditHeader)

        getCell(COL_CREDIT_DATE).setCellValue(creditInfo[1].toString())
    }

    getRow(ROW_CREDIT_INFO + 1).getCell(COL_CREDIT_DATE).setCellValue(creditInfo[2].toString())
    getRow(ROW_CREDIT_INFO + 2).getCell(COL_CREDIT_DATE).setCellValue(creditInfo[3].toString())

    getRow(ROW_CREDIT_INFO + 3).getCell(COL_CREDIT_DATE).setCellValue(LocalDate.now().formatDateInXlsx())

    getRow(ROW_RATING).getCell(COL_CREDIT_DATE).setCellValue(rating)

    getRow(ROW_CLIENT_LGD).getCell(COL_CREDIT_DATE).setCellValue(lgdRate.toDouble()/100.0)
}

private fun XSSFSheet.setDataForm(data: List<Array<Any?>>) {

    for(rowIndex in ROW_DATE_FORM1+1..END_ROW_FORM1) {

        val cell = getRow(rowIndex).getCell(COL_CODE)?.takeIf { it.cellType == CellType.NUMERIC} ?: continue

        val codeValue = cell.numericCellValue.toInt()

        val (reportValue, yearValue) = data.findValueByCode(codeValue) ?: continue

        reportValue?.let {  getRow(rowIndex).getCell(COL_REPORT).setCellValue(it) }

        yearValue?.let {  getRow(rowIndex).getCell(COL_REPORT_YEAR).setCellValue(it) }
    }
}

private fun  List<Array<Any?>>.findValueByCode(code: Int): Pair<Double?, Double?>? {
    val row = firstOrNull { (it[0] as? Number)?.toInt() == code } ?: return null

    return Pair((row[1] as? Number)?.toDouble(), (row[2] as? Number)?.toDouble())
}


private fun XSSFSheet.setReportDate(reportDate: LocalDate) {

    val startYear = reportDate.minusDays(1).withDayOfYear(1)

    val diffQuarter = IsoFields.QUARTER_YEARS.between(startYear, reportDate)

    getRow(ROW_DIFF_QUARTER).getCell(COL_CODE).setCellValue(diffQuarter.toDouble())

    with(getRow(ROW_DATE_FORM1)) {
        getCell(COL_REPORT).setCellValue(reportDate.formatDateInXlsx())

        getCell(COL_REPORT_YEAR).setCellValue(startYear.formatDateInXlsx())
    }
}

private const val ROW_CREDIT_INFO = 1

private const val COL_CREDIT_DATE = 1

private const val ROW_CLIENT_NAME = 0

private const val ROW_CLIENT_LGD = 28

private const val ROW_RATING = 16

private const val COL_CLIENT_NAME = 0

private const val ROW_DIFF_QUARTER = 76

private const val ROW_DATE_FORM1 = 78

private const val END_ROW_FORM1 = 136

private const val COL_CODE = 1

private const val COL_REPORT = 2

private const val COL_REPORT_YEAR = 3

private const val SELECT_DATA_FORM1 = "{ ? = call od.PTKB_LOAN_METHOD_JUR.getDataFormForXlsx(?, ?) }"

private const val SELECT_CREDIT_INFO = "{ ? = call od.PTKB_LOAN_METHOD_JUR.getCreditInfo( ? ) }"

private const val SELECT_CREDIT_INFO_LOAN = "{ ? = call od.PTKB_LOAN_METHOD_JUR.getCreditInfoByLoan( ?, ? ) }"

private const val SELECT_LGD_TYPES = "{ ? = call od.PTKB_LOAN_METHOD_JUR.getMsfoLgdTypeRate }"

private const val SELECT_LGD_RATE = "select od.PTKB_LOAN_METHOD_JUR.getRateByType(?, ?) from dual"

private fun LocalDateTime.formatDateTime() = DateTimeFormatter.ofPattern("MMdd-HHmmss").format(this)