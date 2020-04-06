package ru.barabo.loan.metodix.gui

import java.awt.BorderLayout
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.JTabbedPane

class TabBook : JPanel() {

    init {
        layout = BorderLayout()

        add(ToolBarBook(), BorderLayout.NORTH)

        add(JTabbedPane(JTabbedPane.TOP).apply {

            addTab(TableBookForm1.NAME_FORM, JScrollPane(TableBookForm1) )

            addTab(TableBookForm2.NAME_FORM, JScrollPane(TableBookForm2) )

        }, BorderLayout.CENTER)
    }

    companion object {
        const val TITLE = "Бухгалтерские формы"
    }
}