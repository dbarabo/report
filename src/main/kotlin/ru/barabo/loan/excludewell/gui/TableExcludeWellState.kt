package ru.barabo.loan.excludewell.gui

import ru.barabo.gui.swing.cross.CrossColumn
import ru.barabo.gui.swing.cross.CrossColumns
import ru.barabo.gui.swing.cross.CrossTable
import ru.barabo.gui.swing.processShowError
import ru.barabo.loan.excludewell.entity.ExcludeWellState
import ru.barabo.loan.excludewell.service.ExcludeWellStateService
import ru.barabo.loan.quality.gui.RemarkEditor
import ru.barabo.loan.quality.gui.remarkMonth
import ru.barabo.loan.quality.gui.valueMonth
import ru.barabo.loan.ratingactivity.gui.CheckBoxEditor
import ru.barabo.loan.ratingactivity.gui.CrossRendererAutoHeight
import javax.swing.table.TableCellEditor
import kotlin.reflect.KMutableProperty1

class TableExcludeWellState : CrossTable<ExcludeWellState>( crossExcludeWellStateColumns, ExcludeWellStateService,
    CrossRendererAutoHeight(crossExcludeWellStateColumns, ExcludeWellStateService)
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

            val propColumn = crossColumns.columns[selectedColumn].prop as KMutableProperty1<ExcludeWellState, Any?>

            ExcludeWellStateService.pasteFromTemplate(propColumn)
        }
    }

    companion object {
        const val NAME = "Исключение из хор. состояния"
    }
}

private val crossExcludeWellState = arrayOf(
    CrossColumn({ "Обстоятельства" }, ExcludeWellState::name, 40),

    //CrossColumn({ "Шаблон" }, ExcludeWellState::template, 10),

    CrossColumn({ valueMonth() } , ExcludeWellState::valueMonth1, 5 ),

    CrossColumn({ remarkMonth() } , ExcludeWellState::remarkMonth1),

    CrossColumn({ valueMonth(3) }, ExcludeWellState::valueMonth4, 5 ),

    CrossColumn({ remarkMonth( 3) }, ExcludeWellState::remarkMonth4 ),

    CrossColumn({ valueMonth( 6) }, ExcludeWellState::valueMonth7, 5 ),

    CrossColumn({ remarkMonth( 6) }, ExcludeWellState::remarkMonth7 ),

    CrossColumn({ valueMonth(9) }, ExcludeWellState::valueMonth10, 5 ),

    CrossColumn({ remarkMonth(9) }, ExcludeWellState::remarkMonth10 )
)

val crossExcludeWellStateColumns = CrossColumns(1, true, crossExcludeWellState)