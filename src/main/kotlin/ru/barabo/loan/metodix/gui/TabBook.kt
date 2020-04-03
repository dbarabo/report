package ru.barabo.loan.metodix.gui

import java.awt.BorderLayout
import javax.swing.JPanel
import javax.swing.JScrollPane

class TabBook : JPanel() {

    init {
        layout = BorderLayout()

        add(ToolBarBook(), BorderLayout.NORTH)

        add(JScrollPane(TableBookForm1), BorderLayout.CENTER)
    }

    companion object {
        const val TITLE = "Бухгалтерские формы"
    }
}