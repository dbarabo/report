package ru.barabo.loan.ratingactivity.gui

import org.slf4j.LoggerFactory
import ru.barabo.gui.swing.cross.*
import ru.barabo.gui.swing.processShowError
import ru.barabo.loan.quality.gui.RemarkEditor
import ru.barabo.loan.quality.gui.remarkMonth
import ru.barabo.loan.quality.gui.setDefaultColorBorder
import ru.barabo.loan.quality.gui.valueMonth
import ru.barabo.loan.ratingactivity.entity.RatingActivity
import ru.barabo.loan.ratingactivity.service.RatingActivityService
import java.awt.*
import java.awt.font.TextAttribute
import javax.swing.AbstractCellEditor
import javax.swing.JCheckBox
import javax.swing.JTable
import javax.swing.JTextArea
import javax.swing.table.TableCellEditor
import javax.swing.table.TableCellRenderer
import kotlin.reflect.KMutableProperty1

private val logger = LoggerFactory.getLogger(TableRatingActivity::class.java)

class TableRatingActivity : CrossTable<RatingActivity>( crossRatingActivityColumns, RatingActivityService,
    CrossRendererAutoHeight(crossRatingActivityColumns, RatingActivityService)
) {

    private val remarkEditor = RemarkEditor()

    private val checkBoxEditor = CheckBoxEditor()

    override fun getCellEditor(row: Int, column: Int): TableCellEditor {

        return if(column in listOf(1, 3, 5, 7, 9)) remarkEditor
        else checkBoxEditor
    }

    fun pasteFromTemplate() {
        try {
            cellEditor.stopCellEditing()
        } catch (e: Exception){}

        processShowError {

            if(selectedColumn !in listOf(3, 5, 7, 9)) throw Exception("Вставка из шаблона возможно только если текущий столбец Ремарка")

            val propColumn = crossColumns.columns[selectedColumn].prop as KMutableProperty1<RatingActivity, Any?>

            RatingActivityService.pasteFromTemplate(propColumn)
        }
    }

    companion object {
        const val NAME = "Оценка реальной деят-ти"
    }
}

private val columnsRatingActivity = arrayOf(
    CrossColumn({ "Наименование показателя" }, RatingActivity::name, 40),

    CrossColumn({ "Шаблон" }, RatingActivity::template, 10),

    CrossColumn({ valueMonth() } , RatingActivity::valueMonth1, 5 ),

    CrossColumn({ remarkMonth() } , RatingActivity::remarkMonth1),

    CrossColumn({ valueMonth(3) }, RatingActivity::valueMonth4, 5 ),

    CrossColumn({ remarkMonth( 3) }, RatingActivity::remarkMonth4 ),

    CrossColumn({ valueMonth( 6) }, RatingActivity::valueMonth7, 5 ),

    CrossColumn({ remarkMonth( 6) }, RatingActivity::remarkMonth7 ),

    CrossColumn({ valueMonth(9) }, RatingActivity::valueMonth10, 5 ),

    CrossColumn({ remarkMonth(9) }, RatingActivity::remarkMonth10 )
)

val crossRatingActivityColumns = CrossColumns(1, true, columnsRatingActivity)

internal class CrossRendererAutoHeight(private val crossColumns: CrossColumns<*>, private val crossData: CrossData<*>)
    : TableCellRenderer {

    private val rowHeight: Int

    private val textOnly =  JTextArea().apply {
        wrapStyleWord = true
        lineWrap = true

        rowHeight = this.preferredSize.height + 1
    }

    private val checkBox = JCheckBox()

    override fun getTableCellRendererComponent(table: JTable, value: Any?,
                                               isSelected: Boolean, hasFocus: Boolean, row: Int, column: Int): Component {

        return if(crossData.getRowType(row) == RowType.SIMPLE &&
            crossColumns.columns[column].prop.returnType.toString().indexOf(".Int") >= 0) {

            checkBoxRenderer(table, value, isSelected, hasFocus, row, column)
        } else {
            textRenderer(table, value, isSelected, hasFocus, row, column)
        }
    }

    private fun checkBoxRenderer(table: JTable, value: Any?,
                                    isSelected: Boolean, hasFocus: Boolean, row: Int, column: Int): Component {

        checkBox.setDefaultColorBorder(table, isSelected, hasFocus, row, column)

        checkBox.isSelected = ( (value as? Number)?.toInt() == 1)

        return checkBox
    }

    private fun textRenderer(table: JTable, value: Any?,
                             isSelected: Boolean, hasFocus: Boolean, row: Int, column: Int): Component {

        textOnly.setDefaultColorBorder(table, isSelected, hasFocus, row, column)

        when(crossData.getRowType(row) ) {
            RowType.SIMPLE -> {
                textOnly.background = if(crossColumns.fixedCount > column) MORE_LIGHT_GRAY else table.background

                textOnly.font = table.font.deriveFont(table.font.style)

                textOnly.isOpaque = true
            }

            RowType.SUM -> {
                textOnly.background = MORE_LIGHT_GRAY //if(crossColumns.fixedCount > column) MORE_LIGHT_GRAY else Color.GRAY

                textOnly.font = table.font.deriveFont(table.font.style or Font.ITALIC or Font.BOLD)

                textOnly.isOpaque = true
            }

            RowType.HEADER -> {
                textOnly.font = table.font.deriveFont(table.font.style or Font.BOLD).apply {
                    deriveFont(mapOf(TextAttribute.UNDERLINE to TextAttribute.UNDERLINE_ON))
                }

                textOnly.background = Color.LIGHT_GRAY
            }
        }

        textOnly.text = value?.toString() ?: ""

        checkHeightRowTable(table, row, column)

        return textOnly
    }

    private fun checkHeightRowTable(table: JTable, row: Int, column: Int) {
        if(column != 0) return

        val widthText = textOnly.getFontMetrics(textOnly.font).stringWidth(textOnly.text)
        val heightScreen6 = Toolkit.getDefaultToolkit().screenSize.width / 6

        val crLfCount = textOnly.text?.count { it == '\n' } ?: 0

        val widthCell = table.columnModel.getColumn(0).width - 6

        val rowCount = widthText / widthCell + 1 + crLfCount

        val cellHeight = rowHeight * rowCount

        val maxHeigh = if(cellHeight > heightScreen6) heightScreen6 else cellHeight

        if(table.getRowHeight(row) != maxHeigh) {
            table.setRowHeight(row, maxHeigh)
        }
    }
}

internal class CheckBoxEditor : AbstractCellEditor(), TableCellEditor {

    private val checkBox = JCheckBox()

    override fun getTableCellEditorComponent(table: JTable?, value: Any?, isSelected: Boolean,
                                             row: Int, column: Int): Component {


        checkBox.isSelected = ( (value as? Number)?.toInt() == 1)

        return checkBox
    }

    override fun getCellEditorValue(): Any = if(checkBox.isSelected)1 else 0
}
