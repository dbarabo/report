package ru.barabo.loan.threateningtrend.gui

import ru.barabo.gui.swing.cross.CrossColumn
import ru.barabo.gui.swing.cross.CrossColumns
import ru.barabo.gui.swing.cross.CrossTable
import ru.barabo.gui.swing.processShowError
import ru.barabo.loan.quality.gui.RemarkEditor
import ru.barabo.loan.quality.gui.remarkMonth
import ru.barabo.loan.quality.gui.valueMonth
import ru.barabo.loan.ratingactivity.gui.CheckBoxEditor
import ru.barabo.loan.ratingactivity.gui.CrossRendererAutoHeight
import ru.barabo.loan.threateningtrend.entity.ThreateningTrend
import ru.barabo.loan.threateningtrend.service.ThreateningTrendService
import javax.swing.table.TableCellEditor
import kotlin.reflect.KMutableProperty1

class TableThreateningTrend : CrossTable<ThreateningTrend>( crossThreateningTrendColumns, ThreateningTrendService,
    CrossRendererAutoHeight(crossThreateningTrendColumns, ThreateningTrendService)
) {

    private val remarkEditor = RemarkEditor()

    private val checkBoxEditor = CheckBoxEditor()

    override fun getCellEditor(row: Int, column: Int): TableCellEditor {

        return if(column in listOf(1, 3, 5, 7)) checkBoxEditor
        else remarkEditor
    }

    fun pasteFromTemplate() {
        try {
            cellEditor.stopCellEditing()
        } catch (e: Exception){}

        processShowError {

            if(selectedColumn !in listOf(3, 5, 7, 9)) throw Exception("Вставка из шаблона возможно только если текущий столбец Ремарка")

            val propColumn = crossColumns.columns[selectedColumn].prop as KMutableProperty1<ThreateningTrend, Any?>

            ThreateningTrendService.pasteFromTemplate(propColumn)
        }
    }

    companion object {
        const val NAME = "Угрожающие тенденции"
    }
}

private val columnsThreateningTrend = arrayOf(
    CrossColumn({ "Тенденция" }, ThreateningTrend::name, 40),

    //CrossColumn({ "Шаблон" }, ThreateningTrend::template, 10),

    CrossColumn({ valueMonth() } , ThreateningTrend::valueMonth1, 5 ),

    CrossColumn({ remarkMonth() } , ThreateningTrend::remarkMonth1),

    CrossColumn({ valueMonth(3) }, ThreateningTrend::valueMonth4, 5 ),

    CrossColumn({ remarkMonth( 3) }, ThreateningTrend::remarkMonth4 ),

    CrossColumn({ valueMonth( 6) }, ThreateningTrend::valueMonth7, 5 ),

    CrossColumn({ remarkMonth( 6) }, ThreateningTrend::remarkMonth7 ),

    CrossColumn({ valueMonth(9) }, ThreateningTrend::valueMonth10, 5 ),

    CrossColumn({ remarkMonth(9) }, ThreateningTrend::remarkMonth10 )
)

val crossThreateningTrendColumns = CrossColumns(1, true, columnsThreateningTrend)
