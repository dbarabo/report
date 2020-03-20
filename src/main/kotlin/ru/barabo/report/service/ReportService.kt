package ru.barabo.report.service

import ru.barabo.afina.AfinaOrm
import ru.barabo.afina.AfinaQuery
import ru.barabo.db.EditType
import ru.barabo.db.annotation.ParamsSelect
import ru.barabo.db.service.StoreFilterService
import ru.barabo.report.entity.Directory
import ru.barabo.report.entity.Report
import ru.barabo.report.entity.StateReport
import ru.barabo.report.entity.defaultTemplateDirectory
import ru.barabo.report.service.DirectoryService.findGroupByDirectoryId
import ru.barabo.xls.ExcelSql
import java.io.File
import java.sql.Timestamp
import java.util.*
import kotlin.collections.ArrayList

object ReportService : StoreFilterService<Report>(AfinaOrm, Report::class.java), ParamsSelect {

    private var directoryId: Long? = null

    var selectedReport: Report? = null
    private set

    override fun selectParams(): Array<Any?>? =
        arrayOf(AfinaQuery.getUserDepartment().workPlaceId, directoryId, AfinaQuery.getUserDepartment().workPlaceId)

    fun reportsByDirectory(directoryId: Long?): List<Report> {
        this.directoryId = directoryId

        initData()

        return dataList.toList()
    }

    fun createNewReport(nameReport: String?, directory: Directory, state: StateReport, templateFile: File) {

        val newReport = Report(directory = directory.id,
            name = nameReport!!,
            fileName = templateFile.name,
            creator = AfinaQuery.getUserDepartment().userId,
            updater = AfinaQuery.getUserDepartment().userId,
            templateFile = templateFile)

        save(newReport)
        newReport.change(nameReport, directory, state, templateFile)

        DirectoryService.initData()
        DirectoryService.selectedDirectory = findGroupByDirectoryId(directory.id!!)
    }

    fun updateReport(report: Report) {

        val selectedDirectory = DirectoryService.selectedDirectory
        report.updater = AfinaQuery.getUserDepartment().userId
        report.updated = Timestamp(Date().time)

        if(report.creator.isBlank()) {
            report.creator = AfinaQuery.getUserDepartment().userId
        }

        save(report)
        DirectoryService.initData()
        DirectoryService.selectedDirectory = selectedDirectory
    }

    fun prepareRun(report: Report): ExcelSql {
        selectedReport = report

        val template = report.getTemplate()

        sentRefreshAllListener(EditType.CHANGE_CURSOR)

        return ExcelSql(template, AfinaQuery, ::generateNewFile)
    }

    fun compileReport(report: Report) {
        val template = report.getTemplate()

        val tempFile = File("${defaultTemplateDirectory()}/3478${template.name}")
        if(tempFile.exists()) tempFile.delete()

        val excelSql = ExcelSql(template, AfinaQuery) {
            tempFile
        }
        excelSql.initRowData( ArrayList() )

        tempFile.delete()
    }

    private fun generateNewFile(template: File): File {

        val report = selectedReport ?: throw Exception("selected report is not found")

        val historyRunNew = HistoryRunService.createHistoryRun(report)

        return File(historyRunNew.fileName)
    }
}

