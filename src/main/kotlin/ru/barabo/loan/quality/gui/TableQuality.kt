package ru.barabo.loan.quality.gui

import ru.barabo.gui.swing.cross.*
import ru.barabo.loan.metodix.service.yearDate
import ru.barabo.loan.quality.entity.Quality
import ru.barabo.loan.quality.service.QualityService
import java.awt.Color
import java.awt.Component
import java.awt.Font
import java.awt.GridLayout
import java.awt.font.TextAttribute
import java.sql.Timestamp
import java.time.format.DateTimeFormatter
import javax.swing.*
import javax.swing.table.TableCellEditor
import javax.swing.table.TableCellRenderer


class TableQuality : CrossTable<Quality>( crossQualityColumns, QualityService,
    CrossRendererAutoHeight(crossQualityColumns, QualityService)) {

    private val remarkEditor = RemarkEditor()

    private val radioEditor = QualityRadioEditor(QualityService)

    override fun getCellEditor(row: Int, column: Int): TableCellEditor {

        return if(column in listOf(2, 4, 6, 8)) remarkEditor
        else radioEditor //super.getCellEditor(row, column)
    }

    companion object {
        const val NAME = "Качественные показатели"
    }
}

private val columnsQuality = arrayOf(
    CrossColumn({ "Наименование показателя" }, Quality::name, 40),

    CrossColumn({ valueMonth() } , Quality::valueMonth1, 5 ),

    CrossColumn({ remarkMonth() } , Quality::remarkMonth1),

    CrossColumn({ valueMonth(3) }, Quality::valueMonth4, 5 ),

    CrossColumn({ remarkMonth( 3) }, Quality::remarkMonth4 ),

    CrossColumn({ valueMonth( 6) }, Quality::valueMonth7, 5 ),

    CrossColumn({ remarkMonth( 6) }, Quality::remarkMonth7 ),

    CrossColumn({ valueMonth(9) }, Quality::valueMonth10, 5 ),

    CrossColumn({ remarkMonth(9) }, Quality::remarkMonth10 )
)

internal fun valueMonth(addMonth: Long = 0L) = formatShortAdd(yearDate, addMonth)

internal fun remarkMonth(addMonth: Long = 0L) = "Ремарка ${formatShortAdd(yearDate, addMonth)}"

val crossQualityColumns = CrossColumns(1, true, columnsQuality)

private val shortYearFormatter = DateTimeFormatter.ofPattern("MM.yy")

private fun formatShortAdd(yearDate: Timestamp, addMonth: Long = 0L): String =
    shortYearFormatter.format(yearDate.toLocalDateTime().plusMonths(addMonth) )

internal class RemarkEditor : AbstractCellEditor(), TableCellEditor {

    private val textOnly =  JTextArea().apply {
        wrapStyleWord = true
        lineWrap = true
    }

    override fun getTableCellEditorComponent(table: JTable?, value: Any?,
                                             isSelected: Boolean, row: Int, column: Int): Component {
        textOnly.text = value?.toString()

        return textOnly
    }

    override fun getCellEditorValue(): Any = textOnly.text
}

private class QualityRadioEditor(private val crossData: CrossData<Quality>) : AbstractCellEditor(), TableCellEditor {

    private val radioPanel =  RadioPanel()

    override fun getTableCellEditorComponent(table: JTable?, value: Any?, isSelected: Boolean,
                                             row: Int, column: Int): Component {


        val dataList = crossData.getEntity(row)?.ballList?.split(';')

        radioPanel.updateValue(dataList, value)

        return radioPanel
    }

    override fun getCellEditorValue(): Any? = radioPanel.getRadioIntValue()
}

class RadioPanel(count: Int = 3) : JPanel() {

    private val radios = Array(count) { JRadioButton() }

    private val stubRadio = JRadioButton()

    init {
        layout = GridLayout(0, 1)

        ButtonGroup().apply {
            radios.forEach {  add(it) }

            add(stubRadio)
        }

        radios.forEach { add(it) }
    }

    fun updateValue(valueList: List<String>?, value: Any?) {

        val text = if(value?.toString().isNullOrBlank()) "@!#!" else value.toString()

        var isAnySelected = false
        for((index, radio) in radios.withIndex()) {
            radio.text = if((valueList?.size ?: 0) > index)valueList?.get(index) else ""
            radio.isSelected = (text == radio.text)
            isAnySelected = isAnySelected || (text == radio.text)
        }

        if(!isAnySelected) {
            stubRadio.isSelected = true
        }
    }

    fun getRadioIntValue(): Any? {

        val selected = radios.firstOrNull { it.isSelected } ?: return null

        return selected.text?.toIntOrNull()
    }
}

private class CrossRendererAutoHeight(private val crossColumns: CrossColumns<Quality>, private val crossData: CrossData<Quality>)
    : TableCellRenderer {

    private val textOnly =  JTextArea().apply {
        wrapStyleWord = true
        lineWrap = true
    }

    private val radioPanel = RadioPanel()

    override fun getTableCellRendererComponent(table: JTable, value: Any?,
                                               isSelected: Boolean, hasFocus: Boolean, row: Int, column: Int): Component {

        return if(crossData.getRowType(row) == RowType.SIMPLE &&
            crossColumns.columns[column].prop.returnType.toString().indexOf(".Int") >= 0) {
            radioButtonRenderer(table, value, isSelected, hasFocus, row, column)
        } else {
            textRenderer(table, value, isSelected, hasFocus, row, column)
        }
    }

    private fun radioButtonRenderer(table: JTable, value: Any?,
                             isSelected: Boolean, hasFocus: Boolean, row: Int, column: Int): Component {

        radioPanel.setDefaultColorBorder(table, isSelected, hasFocus, row, column)

        val dataList = crossData.getEntity(row)?.ballList?.split(';')
        radioPanel.updateValue(dataList, value)

        return radioPanel
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

        if(column == 0) {
            table.setRowHeight(row, textOnly.preferredSize.height)
        }

        return textOnly
    }
}

internal fun JComponent.setDefaultColorBorder(table: JTable, isSelected: Boolean, hasFocus: Boolean, row: Int, column: Int) {

    isOpaque = true

    if (hasFocus) {
        border = UIManager.getBorder("Table.focusCellHighlightBorder")
        if (table.isCellEditable(row, column)) {
            foreground = UIManager.getColor("Table.focusCellForeground")
            background = UIManager.getColor("Table.focusCellBackground")
        }
    } else {
        border = UIManager.getBorder("TableHeader.cellBorder")
    }

    if (isSelected) {
        background = table.selectionBackground
        foreground = table.selectionForeground
    } else {
        background = table.background
        foreground = table.foreground
    }
}