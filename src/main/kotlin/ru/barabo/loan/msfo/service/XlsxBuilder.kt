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


object XlsxBuilder {

    fun processCopy(reportDate: LocalDate) {

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

                    book.processBook(reportDate)

                    book.write(FileOutputStream(savePathFile))
                }
            }
        }
        Desktop.getDesktop().open(savePathFile)
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


private fun XSSFWorkbook.processBook(reportDate: LocalDate) {

    val clientId = ClientBookService.selectedEntity()?.idClient ?: throw Exception("Клиент не выбран")

    val params = arrayOf<Any?>(clientId, Date(Timestamp.valueOf(reportDate.atStartOfDay()).time))

    val data = AfinaQuery.selectCursor(SELECT_DATA_FORM1, params)

    val sheet = this.getSheet("Рейтинг")

    sheet.setReportDate(reportDate)

    sheet.setDataForm(data)
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

    with(getRow(ROW_DATE_FORM1)) {
        getCell(COL_REPORT).setCellValue(reportDate.formatDateInXlsx())

        getCell(COL_REPORT_YEAR).setCellValue(reportDate.withDayOfYear(1).formatDateInXlsx())
    }
}

private const val ROW_DATE_FORM1 = 78

private const val END_ROW_FORM1 = 136

private const val COL_CODE = 1

private const val COL_REPORT = 2

private const val COL_REPORT_YEAR = 3

private const val SELECT_DATA_FORM1 = "{ ? = call od.PTKB_LOAN_METHOD_JUR.getDataFormForXlsx(?, ?) }"

private fun LocalDateTime.formatDateTime() = DateTimeFormatter.ofPattern("MMdd-HHmmss").format(this)