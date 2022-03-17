package ru.barabo.loan.downfactors.gui

import org.slf4j.LoggerFactory
import ru.barabo.gui.swing.cross.CrossColumn
import ru.barabo.gui.swing.cross.CrossColumns
import ru.barabo.gui.swing.cross.CrossTable
import ru.barabo.gui.swing.processShowError
import ru.barabo.loan.downfactors.entity.DownFactors
import ru.barabo.loan.downfactors.service.DownFactorsService
import ru.barabo.loan.quality.gui.RemarkEditor
import ru.barabo.loan.quality.gui.remarkMonth
import ru.barabo.loan.quality.gui.valueMonth
import ru.barabo.loan.ratingactivity.gui.CheckBoxEditor
import ru.barabo.loan.ratingactivity.gui.CrossRendererAutoHeight
import javax.swing.table.TableCellEditor
import kotlin.reflect.KMutableProperty1

private val logger = LoggerFactory.getLogger(TableDownFactors::class.java)

class TableDownFactors : CrossTable<DownFactors>( crossDownFactorsColumns, DownFactorsService,
    CrossRendererAutoHeight(crossDownFactorsColumns, DownFactorsService)
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

            val propColumn = crossColumns.columns[selectedColumn].prop as KMutableProperty1<DownFactors, Any?>

            DownFactorsService.pasteFromTemplate(propColumn)
        }
    }

    companion object {
        const val NAME = "Понижающие факторы"
    }
}

private val crossDownFactors = arrayOf(
    CrossColumn({ " Понижающий фактор" }, DownFactors::name, 40),

    //CrossColumn({ "Шаблон" }, DownFactors::template, 10),

    CrossColumn({ valueMonth() } , DownFactors::valueMonth1, 5 ),

    CrossColumn({ remarkMonth() } , DownFactors::remarkMonth1),

    CrossColumn({ valueMonth(3) }, DownFactors::valueMonth4, 5 ),

    CrossColumn({ remarkMonth( 3) }, DownFactors::remarkMonth4 ),

    CrossColumn({ valueMonth( 6) }, DownFactors::valueMonth7, 5 ),

    CrossColumn({ remarkMonth( 6) }, DownFactors::remarkMonth7 ),

    CrossColumn({ valueMonth(9) }, DownFactors::valueMonth10, 5 ),

    CrossColumn({ remarkMonth(9) }, DownFactors::remarkMonth10 )
)

val crossDownFactorsColumns = CrossColumns(1, true, crossDownFactors)