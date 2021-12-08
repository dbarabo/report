package ru.barabo.loan.metodix.gui

import ru.barabo.gui.swing.ResourcesManager
import ru.barabo.loan.excludewell.gui.TableExcludeWellState
import ru.barabo.loan.excludewell.gui.crossExcludeWellStateColumns
import ru.barabo.loan.quality.gui.TableQuality
import ru.barabo.loan.quality.gui.crossQualityColumns
import ru.barabo.loan.ratingactivity.gui.TableRatingActivity
import ru.barabo.loan.ratingactivity.gui.crossRatingActivityColumns
import ru.barabo.loan.threateningtrend.gui.TableThreateningTrend
import ru.barabo.loan.threateningtrend.gui.crossThreateningTrendColumns
import java.awt.BorderLayout
import javax.swing.JButton
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.JTabbedPane

class TabBook : JPanel() {

    init {
        layout = BorderLayout()

        val tableQuality = TableQuality()

        val tableRatingActivity = TableRatingActivity()

        val tableThreateningTrend = TableThreateningTrend()

        val tableExcludeWellState = TableExcludeWellState()

        val templateRating = JButton("Из шаблона", ResourcesManager.getIcon("paste24")).apply {
            addActionListener { tableRatingActivity.pasteFromTemplate() }
        }

        val toolBar = ToolBarBook(
            listOf(TableBookForm1.crossColumns, crossQualityColumns, crossRatingActivityColumns,
                crossThreateningTrendColumns, crossExcludeWellStateColumns),

            listOf(TableBookForm1, TableBookForm2, tableQuality, tableRatingActivity,
                tableThreateningTrend, tableExcludeWellState))

        add(toolBar, BorderLayout.NORTH)

        add(JTabbedPane(JTabbedPane.TOP).apply {

            addTab(TableBookForm1.NAME_FORM, JScrollPane(TableBookForm1) )

            addTab(TableBookForm2.NAME_FORM, JScrollPane(TableBookForm2) )

            addTab(TableQuality.NAME, JScrollPane(tableQuality) )

            addTab(TableRatingActivity.NAME, JScrollPane(tableRatingActivity) )

            addTab(TableThreateningTrend.NAME, JScrollPane(tableThreateningTrend) )

            addTab(TableExcludeWellState.NAME, JScrollPane(tableExcludeWellState) )


            addChangeListener {
                if(this.selectedIndex == 3) {
                    toolBar.add(templateRating)
                } else {
                    toolBar.remove(templateRating)
                }
                toolBar.revalidate()
            }

        }, BorderLayout.CENTER)
    }

    companion object {
        const val TITLE = "Бухгалтерские формы"
    }
}
