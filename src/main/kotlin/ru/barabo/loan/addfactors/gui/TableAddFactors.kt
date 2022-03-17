package ru.barabo.loan.addfactors.gui

import org.slf4j.LoggerFactory
import ru.barabo.gui.swing.cross.CrossColumn
import ru.barabo.gui.swing.cross.CrossColumns
import ru.barabo.gui.swing.cross.CrossTable
import ru.barabo.gui.swing.processShowError
import ru.barabo.loan.addfactors.entity.AddFactors
import ru.barabo.loan.addfactors.service.AddFactorsService
import ru.barabo.loan.quality.gui.RemarkEditor
import ru.barabo.loan.quality.gui.remarkMonth
import ru.barabo.loan.quality.gui.valueMonth
import ru.barabo.loan.ratingactivity.gui.CheckBoxEditor
import ru.barabo.loan.ratingactivity.gui.CrossRendererAutoHeight
import javax.swing.table.TableCellEditor
import kotlin.reflect.KMutableProperty1

private val logger = LoggerFactory.getLogger(TableAddFactors::class.java)

class TableAddFactors : CrossTable<AddFactors>( crossAddFactorsColumns, AddFactorsService,
    CrossRendererAutoHeight(crossAddFactorsColumns, AddFactorsService)
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
        } catch (e: Exception){
            logger.error("pasteFromTemplate", e)
        }

        processShowError {

            if(selectedColumn !in listOf(3, 5, 7, 9)) throw Exception("Вставка из шаблона возможно только если текущий столбец Ремарка")

            val propColumn = crossColumns.columns[selectedColumn].prop as KMutableProperty1<AddFactors, Any?>

            AddFactorsService.pasteFromTemplate(propColumn)
        }
    }

    companion object {
        const val NAME = "Доп. факторы"
    }
}

private val crossAddFactors = arrayOf(
    CrossColumn({ "Доп.фактор" }, AddFactors::name, 40),

    CrossColumn({ valueMonth() } , AddFactors::valueMonth1, 5 ),

    CrossColumn({ remarkMonth() } , AddFactors::remarkMonth1),

    CrossColumn({ valueMonth(3) }, AddFactors::valueMonth4, 5 ),

    CrossColumn({ remarkMonth( 3) }, AddFactors::remarkMonth4 ),

    CrossColumn({ valueMonth( 6) }, AddFactors::valueMonth7, 5 ),

    CrossColumn({ remarkMonth( 6) }, AddFactors::remarkMonth7 ),

    CrossColumn({ valueMonth(9) }, AddFactors::valueMonth10, 5 ),

    CrossColumn({ remarkMonth(9) }, AddFactors::remarkMonth10 )
)

val crossAddFactorsColumns = CrossColumns(1, true, crossAddFactors)