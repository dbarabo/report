package ru.barabo.report.gui

import ru.barabo.afina.AccessMode
import ru.barabo.afina.AfinaQuery
import ru.barabo.gui.swing.*
import ru.barabo.report.main.Report
import ru.barabo.report.service.DirectoryService
import ru.barabo.report.service.ReportService
import java.lang.Exception
import javax.swing.JToolBar

class ToolBarReport(private val mainReport: Report) : JToolBar() {
    init {
        toolButton("refresh", "Обновить") { refreshData() }

        popupButton("Создать ➧", "newFile") {
            menuItem("Папку", "folder") {
                DialogCreateDirectory(null, this).showDialogResultOk()
            }

            menuItem("Отчет", "exportXLS") {
                DialogCreateReport(null, this).showDialogResultOk()
            }
        }.apply {
            isEnabled = (!mainReport.isOnlyReport) && (AfinaQuery.getUserDepartment().accessMode == AccessMode.FullAccess)

            isVisible = isEnabled
        }

        popupButton("Правка ➧", "application") {
            menuItem("Папки", "folder") {
                DialogCreateDirectory(DirectoryService.selectedDirectory?.directory, this).showDialogResultOk()
            }

            menuItem("Отчета", "exportXLS") {
                DialogCreateReport(ReportService.selectedReport, this).showDialogResultOk()
            }
        }.apply {
            isEnabled = (!mainReport.isOnlyReport) && (AfinaQuery.getUserDepartment().accessMode == AccessMode.FullAccess)

            isVisible = isEnabled
        }

        val access = toolButton("readonly", "Доступы") { showAccess() }
        access?.isEnabled = (!mainReport.isOnlyReport) && (AfinaQuery.getUserDepartment().accessMode == AccessMode.FullAccess)
        access?.isVisible = access!!.isEnabled

        onOffButton("Режим отчета", mainReport.isOnlyReport) {
            mainReport.isOnlyReport = !mainReport.isOnlyReport
            refreshData()
        }
    }

    private fun refreshData() {
        DirectoryService.isOnlyWork = mainReport.isOnlyReport
        // DirectoryService.initData()
    }

    private fun showAccess() {
        processShowError {
            if(ReportService.selectedReport?.id == null) throw Exception("Сначала выберите отчет для установки для него доступов")

            DialogAccessReport(this).showDialogResultOk()
        }
    }
}