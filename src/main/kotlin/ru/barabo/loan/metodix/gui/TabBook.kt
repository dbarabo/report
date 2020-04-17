package ru.barabo.loan.metodix.gui

import ru.barabo.loan.quality.gui.TableQuality
import ru.barabo.loan.quality.gui.crossQualityColumns
import java.awt.BorderLayout
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.JTabbedPane

class TabBook : JPanel() {

    init {
        layout = BorderLayout()

        val tableQuality = TableQuality()

        add(ToolBarBook(listOf(TableBookForm1.crossColumns, crossQualityColumns),
            listOf(TableBookForm1, TableBookForm2, tableQuality)
        ), BorderLayout.NORTH)

        add(JTabbedPane(JTabbedPane.TOP).apply {

            addTab(TableBookForm1.NAME_FORM, JScrollPane(TableBookForm1) )

            addTab(TableBookForm2.NAME_FORM, JScrollPane(TableBookForm2) )

            addTab(TableQuality.NAME, JScrollPane(tableQuality) )

        }, BorderLayout.CENTER)
    }

    companion object {
        const val TITLE = "Бухгалтерские формы"
    }
}