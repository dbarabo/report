package ru.barabo.report.main

import org.slf4j.LoggerFactory
import ru.barabo.afina.AccessMode
import ru.barabo.afina.AfinaQuery
import ru.barabo.afina.AfinaQuery.getUserDepartment
import ru.barabo.afina.AfinaQuery.isTestBaseConnect
import ru.barabo.afina.VersionChecker
import ru.barabo.afina.gui.ModalConnect
import ru.barabo.gui.swing.ResourcesManager
import ru.barabo.gui.swing.mainBook
import ru.barabo.gui.swing.processShowError
import ru.barabo.loan.metodix.gui.TabBook
import ru.barabo.report.gui.TabReport
import java.awt.BorderLayout
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import java.io.File
import java.net.InetAddress
import javax.swing.JFrame
import javax.swing.JTabbedPane
import kotlin.concurrent.thread
import kotlin.system.exitProcess

private val logger = LoggerFactory.getLogger("Report")!!

fun main() {

    Report()
}

class Report : JFrame() {

    private var _isOnlyReport: Boolean = false

    private lateinit var mainBook: JTabbedPane

    var isOnlyReport: Boolean
        get() = _isOnlyReport

        set(value) {
            _isOnlyReport = value

            rebuildGui()
        }

    init {
        if(!ModalConnect.initConnect(this)) {
            exitProcess(0)
        }

        var isOk = false

        processShowError {
            AfinaQuery.execute(query = CHECK_WORKPLACE, params = null)

            isOk = true
        }

        if(!isOk) {
            exitProcess(0)
        }

        buildGui()
    }

    private fun buildGui() {

        layout = BorderLayout()

        title = title()
        iconImage = ResourcesManager.getIcon("report")?.image

        mainBook = buildMainBook()

        add( mainBook, BorderLayout.CENTER)

        defaultCloseOperation = EXIT_ON_CLOSE
        isVisible = true

        pack()
        extendedState = MAXIMIZED_BOTH

        VersionChecker.runCheckVersion("REPORT.JAR", 11)

        addWindowListener(object : WindowAdapter() {
            override fun windowClosing(e: WindowEvent?) {
                VersionChecker.exitCheckVersion()
            }
        })
    }

    private fun rebuildGui() {
        if(!::mainBook.isInitialized) {
            buildGui()
            return
        }

        logger.error("rebuildGui mainBook=$mainBook")

        mainBook.removeAll()

        if(isShowCatalog() ) {
            mainBook.addTab(TabBook.TITLE, TabBook() )
        }

        mainBook.addTab(TabReport.TITLE, TabReport(this) )

        mainBook.invalidate()
        mainBook.repaint()
    }

    private fun buildMainBook(): JTabbedPane {

        return JTabbedPane(JTabbedPane.TOP).apply {

            if(isShowCatalog() ) {
                addTab(TabBook.TITLE, TabBook() )
            }

            addTab(TabReport.TITLE, TabReport(this@Report) )
        }
    }

    private fun title(): String {
        val (userName, departmentName, workPlace, _, userId, _) = getUserDepartment()

        val user = userName ?: userId

        val db = if (isTestBaseConnect()) "TEST" else "AFINA"

        val header = if(isShowCatalog()) "Отчеты и Справочники:" else "Отчеты:"

        return "$header [$db] [$user] [$departmentName] [$workPlace]"
    }

    private fun isShowCatalog(): Boolean =
        (!isOnlyReport) && (getUserDepartment().accessMode in listOf(AccessMode.FullAccess, AccessMode.CreditAccess) )
}

private const val CHECK_WORKPLACE = "{ call od.XLS_REPORT_ALL.checkWorkplace }"

